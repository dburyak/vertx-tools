package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
import com.dburyak.vertx.core.util.Tuple;
import com.dburyak.vertx.core.util.Tuple2;
import com.dburyak.vertx.gcp.ProjectIdProvider;
import com.dburyak.vertx.gcp.pubsub.DeliverableMsg;
import com.dburyak.vertx.gcp.pubsub.PubSub;
import com.dburyak.vertx.gcp.pubsub.PubSubUtil;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ExpirationPolicy;
import com.google.pubsub.v1.Subscription;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedList;

/**
 * Config store implementation for GCP Secret Manager.
 * <p>
 * This class is instantiated via SPI mechanism (from {@link GcpSecretManagerConfigStoreSpiFactory}) outside of DI
 * container, by direct constructor call. Therefore, all the collaborator beans are injected via setters after
 * instantiation by DI container explicit injection.
 */
@Singleton
@Slf4j
public class GcpSecretManagerConfigStore implements ConfigStore {
    private static final Duration MSG_RETENTION_DURATION = Duration.ofMinutes(10);
    private static final Duration SUB_INACTIVITY_EXPIRATION = Duration.ofDays(1);
    private static final Duration MSG_ACK_DEADLINE = Duration.ofSeconds(10);
    private static final String POD_NAME_ENV = "POD_NAME";
    private static final String POD_NAME_SYS_PROP = "pod.name";
    private static final String ATTR_KEY_EVENT_TYPE = "eventType";
    private static final String ATTR_KEY_SECRET_ID = "secretId";
    private static final Set<String> SECRET_UPD_EVENT_TYPES = Set.of("SECRET_VERSION_ADD");

    /**
     * Config store type for registering in vertx.
     */
    public static final String TYPE = GcpSecretManagerConfigStoreSpiFactory.NAME;

    // it's safe to have only volatile here as config store is never called concurrently by vertx, but may be called
    // from different EL threads
    private volatile GcpSecretManagerConfigProperties cfg;
    private volatile GcpSecretManager secretManager;
    private volatile PubSub pubSub;
    private volatile SubscriptionAdminClient subscriptionAdminClient;
    private volatile PubSubUtil pubSubUtil;
    private volatile SecretManagerUtil gsmUtil;
    private volatile Scheduler elScheduler;
    private volatile Scheduler blockingScheduler;
    private volatile String projectId;
    private volatile JsonObject cachedSecretOpts;
    private volatile Map<String, List<String>> secretShortNamesToConfigOpts = new HashMap<>();
    private volatile Map<String, List<String>> secretFqnNamesToConfigOpts = new HashMap<>();
    private volatile Instant lastRefreshedAt;
    private volatile Disposable gsmUpdNotificationSubscriber;
    private final Collection<Subscription> notificationSubscriptions = synchronizedList(new ArrayList<>());
    private final AtomicInteger subscriptionSuffixCounter = new AtomicInteger();
    private final int rndInstanceId = new Random().nextInt(Integer.MAX_VALUE);
    private String hostname = null;
    private final Object hostnameLock = new Object();


    @Override
    public Future<Buffer> get() {
        // avoid extra volatile reads
        var cfgRef = cfg;
        var lastRefreshedAtRef = lastRefreshedAt;

        Single<JsonObject> secretsFuture;
        if (lastRefreshedAtRef == null || isCacheOutdated(lastRefreshedAtRef, cfgRef)) { // fetch
            secretsFuture = retrieveAndCacheSecrets();
        } else { // use cached
            var cachedSecretOptsRef = cachedSecretOpts;
            log.debug("using cached gsm secret options: numSecrets={}, lastRefreshedAt={}",
                    cachedSecretOptsRef.size(), lastRefreshedAtRef);
            secretsFuture = Single.just(cachedSecretOptsRef);
        }
        var result = Promise.<Buffer>promise();
        secretsFuture.subscribe(json -> result.complete(Buffer.buffer(json.encode())),
                err -> {
                    log.error("gsm secrets retrieval failed: err={}", err.toString());
                    result.fail(err);
                });
        return result.future();
    }

    @PostConstruct
    public void init() {
        // avoid extra volatile reads
        var cfgRef = cfg;
        var projectIdRef = projectId;
        log.debug("init gsm config store: proj={}", projectIdRef);
        if (!cfgRef.isEnabled()) {
            return;
        }
        initMappings(cfgRef);
        if (!cfgRef.isPubsubNotificationsEnabled()) {
            return;
        }
        var subscriptionAdminClientRef = subscriptionAdminClient;
        var secretManagerRef = secretManager;
        gsmUpdNotificationSubscriber = Observable.fromIterable(cfgRef.getSecretConfigOptions())
                .map(opt -> Tuple.of(opt, evaluateNotificationTopicForOption(cfgRef.getPubsubNotificationTopic(), opt)))
                .filter(t2 -> t2.getV2() != null && !t2.getV2().isBlank())
                .map(t2 -> {
                    var cfgOpt = t2.getV1();
                    var topic = t2.getV2();
                    var proj = evaluateProjectForOption(projectIdRef, cfgRef, cfgOpt);
                    // secret and topic may be in different projects, but subscriptions will be created/deleted in this
                    // project, so managing subscriptions won't require cross-project "pubsub editor" role
                    var fqnTopic = pubSubUtil.ensureFqnTopic(proj, topic);
                    return Tuple.of(cfgOpt, fqnTopic);
                })
                .distinct(Tuple2::getV2) // distinct by fqnTopic, we need single subscription per topic
                .flatMapCompletable(t -> {
                    var cfgOpt = t.getV1();
                    var fqnTopic = t.getV2();
                    var shortTopicName = pubSubUtil.topicShortName(fqnTopic);
                    var fqnSub = pubSubUtil.fqnSubscription(projectIdRef, shortTopicName + "-" + subscriptionSuffix());
                    return createSubscriptionAndSubscribe(fqnTopic, fqnSub, cfgOpt, subscriptionAdminClientRef,
                            secretManagerRef);
                })
                .subscribe(() -> log.debug("gsm config update listener initialized"),
                        err -> log.error("gsm config update failed", err));
    }

    @Override
    public Future<Void> close() {
        log.debug("closing gsm config store");
        gsmUpdNotificationSubscriber.dispose();
        var startedAt = Instant.now();
        // parallelize subscription deletion on blockingScheduler, as it can take a while
        Flowable.fromIterable(notificationSubscriptions)
                .parallel().runOn(blockingScheduler)
                .flatMap(subscription -> {
                    var subName = subscription.getName();
                    log.debug("deleting gsm config updates subscription: sub={}", subName);
                    try {
                        subscriptionAdminClient.deleteSubscription(subscription.getName());
                        log.debug("gsm config updates subscription deleted: sub={}", subName);
                        return Flowable.just(subName);
                    } catch (Exception e) {
                        log.error("failed to delete gsm config updates subscription: sub={}", subName, e);
                        return Flowable.error(e);
                    }
                })
                .sequential()
                .toList()
                // need blocking wait here as config stores shutdown is actually synchronous (as of vertx 4.5.1)
                // if we don't wait, then vertx along with vertx rx schedulers and subscriptionAdminClient may be
                // closed before start subscription deletions
                .blockingGet();
        var deletionDuration = Duration.between(startedAt, Instant.now());
        log.debug("gsm config store closed: duration={}, numSubscriptionsDeleted={}",
                deletionDuration, notificationSubscriptions.size());
        notificationSubscriptions.clear();
        return Future.succeededFuture();
    }

    private void initMappings(GcpSecretManagerConfigProperties cfgRef) {
        var shortMappingRef = secretShortNamesToConfigOpts;
        var fqnMappingRef = secretFqnNamesToConfigOpts;
        var projectIdRef = projectId;
        for (var opt : cfgRef.getSecretConfigOptions()) {
            var secretShortName = gsmUtil.secretShortName(opt.getSecretName());
            shortMappingRef.computeIfAbsent(secretShortName, ignr -> new ArrayList<>())
                    .add(opt.getConfigOption());
            var proj = evaluateProjectForOption(projectIdRef, cfgRef, opt);
            var fqnSecretName = gsmUtil.ensureFqnSecret(proj, opt.getSecretName());
            fqnMappingRef.computeIfAbsent(fqnSecretName, ignr -> new ArrayList<>())
                    .add(opt.getConfigOption());
        }
    }

    private Single<JsonObject> retrieveAndCacheSecrets() {
        return retrieveSecrets().doOnSuccess(json -> {
            cachedSecretOpts = json;
            lastRefreshedAt = Instant.now();
        });
    }

    private Single<JsonObject> retrieveSecrets() {
        log.debug("retrieving secrets config options from gsm");
        // avoid extra volatile reads
        var cfgRef = cfg;
        var projectIdRef = projectId;
        var secretManagerRef = secretManager;

        if (cfgRef.getSecretConfigOptions().isEmpty()) {
            return Single.just(new JsonObject());
        }
        var startedAt = Instant.now();
        return Observable.fromIterable(cfgRef.getSecretConfigOptions())
                .flatMapSingle(secretOpt -> {
                    var proj = evaluateProjectForOption(projectIdRef, cfgRef, secretOpt);
                    return secretManagerRef.getSecretString(proj, secretOpt.getSecretName(), null)
                            .map(secretValue -> Map.entry(secretOpt.getConfigOption(), secretValue));
                })
                .toMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnSuccess(m -> log.debug("gsm secret options retrieved: numSecrets={}, duration={}",
                        m.size(), Duration.between(startedAt, Instant.now())))
                .map(m -> {
                    var json = new JsonObject();
                    m.forEach(json::put);
                    return json;
                });
    }

    private Completable createSubscriptionAndSubscribe(String fqnTopic, String fqnSub,
            SecretOptionConfigEntryProperties secretOpt, SubscriptionAdminClient subscriptionAdminClientRef,
            GcpSecretManager secretManagerRef) {
        return Single.fromSupplier(() -> {
                    log.debug("create gsm config updates subscription: opt={}, secret={}, topic={}, sub={}",
                            secretOpt.getConfigOption(), secretOpt.getSecretName(), fqnTopic, fqnSub);
                    return buildCfgUpdNotificationSubscription(subscriptionAdminClientRef, fqnTopic, fqnSub);
                })
                .subscribeOn(blockingScheduler) // makes block above to run on worker scheduler
                .observeOn(elScheduler) // makes all blocks below to run on event loop scheduler
                .doOnSuccess(subscription -> {
                    log.debug("gsm config updates subscription created: sub={}", fqnSub);
                    notificationSubscriptions.add(subscription);
                })
                .ignoreElement().andThen(pubSub.subscribe(fqnSub)
                        .doOnSubscribe(ignr -> log.debug("subscribing to gsm config updates: sub={}", fqnSub)))
                .flatMapCompletable(upd -> {
                    var eventType = getEventType(upd);
                    var updatedSecretId = getSecretId(upd);
                    if (!isSecretValueUpdateEvent(upd)) {
                        log.debug("skip irrelevant gsm update: eventType={}, secretId={}", eventType, updatedSecretId);
                        return upd.delivery().ack();
                    }
                    // update references project by number, but config may reference it by projectId,
                    // so we need to refresh all config options that have same secret short name to not miss updates
                    var optsToRefresh = secretShortNamesToConfigOpts.get(gsmUtil.secretShortName(updatedSecretId));
                    log.debug("received gsm config update notification: secretName={}, cfgOptsToRefresh={}",
                            updatedSecretId, optsToRefresh);
                    var projectIdRef = projectId;
                    var cfgRef = cfg;
                    var cachedSecretOptsRef = cachedSecretOpts;
                    return Observable.fromIterable(optsToRefresh)
                            .flatMapCompletable(optName -> {
                                var opt = getSecretOptionCfgByName(optName, cfgRef);
                                var proj = evaluateProjectForOption(projectIdRef, cfgRef, opt);
                                var secretId = gsmUtil.ensureFqnSecret(proj, opt.getSecretName());
                                return secretManagerRef.getSecretString(secretId)
                                        .doOnSuccess(secretValue -> {
                                            var optsForSecret = secretFqnNamesToConfigOpts.get(secretId);
                                            for (var o : optsForSecret) {
                                                cachedSecretOptsRef.put(o, secretValue);
                                            }
                                            // FIXME: remove secret value from log after debugging
                                            log.debug("gsm config opt updated: secretName={}, optsForSecret={}, " +
                                                    "secretValue={}", secretId, optsForSecret, secretValue);
                                        })
                                        .ignoreElement();
                            })
                            .andThen(upd.delivery().ack());
                });
    }

    private Subscription buildCfgUpdNotificationSubscription(SubscriptionAdminClient subscriptionAdminClientRef,
            String fqnTopic, String fqnSub) {
        return subscriptionAdminClientRef.createSubscription(Subscription.newBuilder()
                .setTopic(fqnTopic)
                .setName(fqnSub)
                .setMessageRetentionDuration(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(MSG_RETENTION_DURATION.toSeconds()))
                .setRetainAckedMessages(false)
                .setExpirationPolicy(ExpirationPolicy.newBuilder()
                        .setTtl(com.google.protobuf.Duration.newBuilder()
                                .setSeconds(SUB_INACTIVITY_EXPIRATION.toSeconds())))
                .setAckDeadlineSeconds((int) MSG_ACK_DEADLINE.toSeconds())
                .build());
    }

    private SecretOptionConfigEntryProperties getSecretOptionCfgByName(String cfgOptionName,
            GcpSecretManagerConfigProperties cfgRef) {
        return cfgRef.getSecretConfigOptions().stream()
                .filter(it -> it.getConfigOption().equals(cfgOptionName))
                .findAny().orElseThrow();
    }

    private boolean isCacheOutdated(Instant lastRefreshedAt, GcpSecretManagerConfigProperties cfg) {
        var now = Instant.now();
        return cfg.isRefreshEnabled() && Duration.between(lastRefreshedAt, now).compareTo(cfg.getRefreshPeriod()) >= 0;
    }

    private String evaluateProjectForOption(String defaultProjectId, GcpSecretManagerConfigProperties cfgRef,
            SecretOptionConfigEntryProperties secretOpt) {
        var proj = secretOpt.getProjectId();
        if (proj == null || proj.isBlank()) {
            proj = cfgRef.getProjectId();
        }
        if (proj == null || proj.isBlank()) {
            proj = defaultProjectId;
        }
        return proj;
    }

    private String evaluateNotificationTopicForOption(String defaultNotificationTopic,
            SecretOptionConfigEntryProperties secretOpt) {
        var topic = secretOpt.getNotificationTopic();
        if (topic == null || topic.isBlank()) {
            topic = defaultNotificationTopic;
        }
        return topic;
    }

    private String subscriptionSuffix() {
        var podName = System.getenv(POD_NAME_ENV);
        if (podName == null || podName.isBlank()) {
            podName = System.getProperty(POD_NAME_SYS_PROP);
        }
        if (podName == null || podName.isBlank()) {
            synchronized (hostnameLock) {
                if (hostname == null) {
                    try {
                        hostname = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        hostname = "listener";
                    }
                }
                podName = hostname;
            }
        }
        return podName + "-" + rndInstanceId + "-" + subscriptionSuffixCounter.getAndIncrement();
    }

    private boolean isSecretValueUpdateEvent(DeliverableMsg secretUpd) {
        return SECRET_UPD_EVENT_TYPES.contains(getEventType(secretUpd))
                && secretShortNamesToConfigOpts.containsKey(getSecretShortName(secretUpd));
    }

    private String getEventType(DeliverableMsg secretUpd) {
        return secretUpd.msg().getAttributesOrDefault(ATTR_KEY_EVENT_TYPE, null);
    }

    private String getSecretId(DeliverableMsg secretUpd) {
        return secretUpd.msg().getAttributesOrDefault(ATTR_KEY_SECRET_ID, null);
    }

    private String getSecretShortName(DeliverableMsg secretUpd) {
        var fqnSecretId = getSecretId(secretUpd);
        return fqnSecretId != null ? gsmUtil.secretShortName(fqnSecretId) : null;
    }

    /**
     * Set GSM config.
     *
     * @param cfg GSM config
     */
    @Inject
    public void setCfg(GcpSecretManagerConfigProperties cfg) {
        this.cfg = cfg;
    }

    /**
     * Set secret manager.
     *
     * @param secretManager secret manager
     */
    @Inject
    public void setSecretManager(GcpSecretManager secretManager) {
        this.secretManager = secretManager;
    }

    /**
     * Set pubsub.
     *
     * @param pubSub pubsub
     */
    @Inject
    public void setPubSub(PubSub pubSub) {
        this.pubSub = pubSub;
    }

    /**
     * Set subscription admin client (for creating/deleting subscriptions).
     *
     * @param subscriptionAdminClient subscription admin client
     */
    @Inject
    public void setSubscriptionAdminClient(SubscriptionAdminClient subscriptionAdminClient) {
        this.subscriptionAdminClient = subscriptionAdminClient;
    }

    /**
     * Set pubsub util.
     *
     * @param pubSubUtil pubsub util
     */
    @Inject
    public void setPubSubUtil(PubSubUtil pubSubUtil) {
        this.pubSubUtil = pubSubUtil;
    }

    /**
     * Set secret manager util.
     *
     * @param secretManagerUtil secret manager util
     */
    @Inject
    public void setSecretManagerUtil(SecretManagerUtil secretManagerUtil) {
        this.gsmUtil = secretManagerUtil;
    }

    /**
     * Set event loop scheduler.
     *
     * @param elScheduler event loop scheduler
     */
    @Inject
    public void setElScheduler(@ForEventLoop Scheduler elScheduler) {
        this.elScheduler = elScheduler;
    }

    /**
     * Set blocking scheduler.
     *
     * @param blockingScheduler blocking scheduler
     */
    @Inject
    public void setBlockingScheduler(@ForWorker Scheduler blockingScheduler) {
        this.blockingScheduler = blockingScheduler;
    }

    /**
     * Set project ID provider.
     *
     * @param projectIdProvider project ID provider
     */
    @Inject
    public void setProjectIdProvider(ProjectIdProvider projectIdProvider) {
        projectId = projectIdProvider.getProjectId();
    }
}
