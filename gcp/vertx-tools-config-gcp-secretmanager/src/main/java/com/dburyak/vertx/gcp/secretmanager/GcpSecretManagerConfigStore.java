package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
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

import javax.annotation.concurrent.GuardedBy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

    // single volatile variable to ensure visibility of reads/writes of all fields below
    private volatile int visibility = 0;
    private int visibilityDummy = 0;
    private GcpSecretManagerConfigProperties cfg;
    private GcpSecretManager secretManager;
    private PubSub pubSub;
    private SubscriptionAdminClient subscriptionAdminClient;
    private PubSubUtil pubSubUtil;
    private SecretManagerUtil gsmUtil;
    private Scheduler elScheduler;
    private Scheduler blockingScheduler;
    private String projectId;
    private Instant lastRefreshedAt;
    private Disposable gsmUpdNotificationSubscriber;
    private final Collection<Subscription> notificationSubscriptions = new ArrayList<>();
    private final Map<String, Set<String>> secretShortNamesToFqnSecretIds = new HashMap<>();
    private final Map<String, Set<String>> fqnSecretIdsToConfigOpts = new HashMap<>();
    private final Map<String, String> fqnTopicToFqnSub = new HashMap<>();
    private int subscriptionSuffixCounter = 0;
    private final int rndInstanceId = new Random().nextInt(Integer.MAX_VALUE);
    private String hostname = null;

    @GuardedBy("cachedSecretOptsLock")
    private JsonObject cachedSecretOpts;
    private final Object cachedSecretOptsLock = new Object();


    @Override
    public Future<Buffer> get() {
        beforeRead(); // ensure visibility
        Single<Buffer> secretsFuture;
        if (lastRefreshedAt == null || isCacheOutdated()) { // fetch
            secretsFuture = retrieveAndCacheSecrets();
        } else { // use cached
            synchronized (cachedSecretOptsLock) {
                log.debug("using cached gsm secret options: numSecrets={}, lastRefreshedAt={}",
                        cachedSecretOpts.size(), lastRefreshedAt);
                secretsFuture = Single.just(cachedSecretOpts.toBuffer());
            }
        }
        var result = Promise.<Buffer>promise();
        secretsFuture.subscribe(result::complete, err -> {
            log.error("gsm secrets retrieval failed: err={}", err.toString());
            result.fail(err);
        });
        return result.future();
    }

    @PostConstruct
    public void init() {
        log.debug("init gsm config store");
        if (!cfg.isEnabled() || !cfg.isPubsubNotificationsEnabled()) {
            return;
        }
        initMappings();
        gsmUpdNotificationSubscriber = Observable.fromIterable(fqnTopicToFqnSub.entrySet())
                .flatMapCompletable(e -> {
                    var fqnTopic = e.getKey();
                    var fqnSub = e.getValue();
                    return createSubscriptionAndSubscribe(fqnTopic, fqnSub);
                })
                .subscribe(() -> { /* subscription for upd events is infinite, completable never completes */ },
                        err -> log.error("gsm config init failed", err));
        afterWrite(); // ensure visibility
    }

    @Override
    public Future<Void> close() {
        beforeRead(); // ensure visibility
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
                // closed before we start subscription deletions
                .blockingGet();
        var deletionDuration = Duration.between(startedAt, Instant.now());
        log.debug("gsm config store closed: duration={}, numSubscriptionsDeleted={}",
                deletionDuration, notificationSubscriptions.size());
        notificationSubscriptions.clear();
        return Future.succeededFuture();
    }

    private void beforeRead() {
        visibilityDummy = visibility; // volatile read
    }

    private void afterWrite() {
        visibility = 0; // volatile write
    }

    private void initMappings() {
        for (var opt : cfg.getSecretConfigOptions()) {
            var secretShortName = gsmUtil.extractSecretName(opt.getSecretName());
            var proj = evaluateProjectForOption(opt);
            var fqnSecretId = gsmUtil.ensureFqnSecret(proj, opt.getSecretName());
            secretShortNamesToFqnSecretIds.computeIfAbsent(secretShortName, k -> new HashSet<>()).add(fqnSecretId);
            fqnSecretIdsToConfigOpts.computeIfAbsent(fqnSecretId, k -> new HashSet<>()).add(opt.getConfigOption());
            var topic = evaluateNotificationTopicForOption(cfg.getPubsubNotificationTopic(), opt);
            if (topic != null && !topic.isBlank()) {
                var fqnTopic = pubSubUtil.ensureFqnTopic(proj, topic);
                fqnTopicToFqnSub.computeIfAbsent(fqnTopic, t -> {
                    var shortTopicName = pubSubUtil.topicShortName(fqnTopic);
                    // secret and topic may be in different projects, but subscriptions will be created/deleted in this
                    // project, so managing subscriptions won't require cross-project "pubsub editor" role
                    return pubSubUtil.fqnSubscription(projectId, shortTopicName + "-" + subscriptionSuffix());
                });
            }
        }
    }

    private Single<Buffer> retrieveAndCacheSecrets() {
        return retrieveSecrets().map(secretsJson -> {
            lastRefreshedAt = Instant.now();
            afterWrite(); // ensure visibility
            synchronized (cachedSecretOptsLock) {
                cachedSecretOpts = secretsJson;
                return cachedSecretOpts.toBuffer();
            }
        });
    }

    private Single<JsonObject> retrieveSecrets() {
        log.debug("retrieving secrets config options from gsm");
        if (cfg.getSecretConfigOptions().isEmpty()) {
            return Single.just(new JsonObject());
        }
        var startedAt = Instant.now();
        return Observable.fromIterable(fqnSecretIdsToConfigOpts.keySet())
                .flatMap(secretId -> {
                    var optNamesForSecretId = fqnSecretIdsToConfigOpts.get(secretId);
                    return secretManager.getSecretString(secretId)
                            .flatMapObservable(secretValue -> Observable.fromIterable(optNamesForSecretId)
                                    .map(optName -> Map.<String, Object>entry(optName, secretValue)));
                })
                .toMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnSuccess(m -> log.debug("gsm secret options retrieved: numSecrets={}, duration={}",
                        m.size(), Duration.between(startedAt, Instant.now())))
                .map(JsonObject::new);
    }

    private Completable createSubscriptionAndSubscribe(String fqnTopic, String fqnSub) {
        return Single.fromSupplier(() -> {
                    log.debug("create gsm config updates subscription: topic={}, sub={}", fqnTopic, fqnSub);
                    return createCfgUpdNotificationSubscription(fqnTopic, fqnSub);
                })
                .subscribeOn(blockingScheduler) // makes block above to run on worker scheduler
                .observeOn(elScheduler) // makes all blocks below to run on event loop scheduler
                .doOnSuccess(subscription -> {
                    log.debug("gsm config updates subscription created: sub={}", fqnSub);
                    notificationSubscriptions.add(subscription);
                    afterWrite(); // ensure visibility
                })
                .ignoreElement().andThen(pubSub.subscribe(fqnSub)
                        .doOnSubscribe(ignr -> log.debug("subscribing to gsm config updates: sub={}", fqnSub)))
                .flatMapCompletable(upd -> {
                    var updatedSecretId = getSecretId(upd);
                    if (!isSecretValueUpdateEvent(upd)) {
                        var eventType = getEventType(upd);
                        log.debug("skip irrelevant gsm update: eventType={}, secretId={}", eventType, updatedSecretId);
                        return upd.delivery().ack();
                    }
                    // update references project by number, but config may reference it by projectId,
                    // so we need to refresh all config options that have same secret *SHORT* name to not miss updates
                    var secretShortName = gsmUtil.extractSecretName(updatedSecretId);
                    log.debug("received gsm config update notification: shortName={}, longName={}",
                            secretShortName, updatedSecretId);
                    return Observable.fromIterable(secretShortNamesToFqnSecretIds.get(secretShortName))
                            .flatMap(secretId -> {
                                var optsForSecretId = fqnSecretIdsToConfigOpts.get(secretId);
                                return secretManager.getSecretString(secretId)
                                        .flatMapObservable(secretValue -> Observable.fromIterable(optsForSecretId)
                                                .map(optName -> Map.<String, String>entry(optName, secretValue)));
                            })
                            .toMap(Map.Entry::getKey, Map.Entry::getValue)
                            .doOnSuccess(updMap -> {
                                log.debug("gsm config opts updated: opts={}", updMap.keySet());
                                synchronized (cachedSecretOptsLock) {
                                    updMap.forEach((optName, optValue) -> cachedSecretOpts.put(optName, optValue));
                                }
                            })
                            .ignoreElement()
                            .andThen(upd.delivery().ack());
                });
    }

    private Subscription createCfgUpdNotificationSubscription(String fqnTopic, String fqnSub) {
        return subscriptionAdminClient.createSubscription(Subscription.newBuilder()
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

    private boolean isCacheOutdated() {
        var now = Instant.now();
        return cfg.isRefreshEnabled() && Duration.between(lastRefreshedAt, now).compareTo(cfg.getRefreshPeriod()) >= 0;
    }

    private String evaluateProjectForOption(SecretOptionConfigEntryProperties secretOpt) {
        var proj = secretOpt.getProjectId();
        if (proj == null || proj.isBlank()) {
            proj = cfg.getProjectId();
        }
        if (proj == null || proj.isBlank()) {
            proj = projectId;
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
            if (hostname == null) {
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    hostname = "listener";
                }
            }
            podName = hostname;
        }
        return podName + "-" + rndInstanceId + "-" + subscriptionSuffixCounter++;
    }

    private boolean isSecretValueUpdateEvent(DeliverableMsg secretUpd) {
        return SECRET_UPD_EVENT_TYPES.contains(getEventType(secretUpd))
                && secretShortNamesToFqnSecretIds.containsKey(getSecretShortName(secretUpd));
    }

    private String getEventType(DeliverableMsg secretUpd) {
        return secretUpd.msg().getAttributesOrDefault(ATTR_KEY_EVENT_TYPE, null);
    }

    private String getSecretId(DeliverableMsg secretUpd) {
        return secretUpd.msg().getAttributesOrDefault(ATTR_KEY_SECRET_ID, null);
    }

    private String getSecretShortName(DeliverableMsg secretUpd) {
        var fqnSecretId = getSecretId(secretUpd);
        return fqnSecretId != null ? gsmUtil.extractSecretName(fqnSecretId) : null;
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
