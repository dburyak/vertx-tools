package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
import com.dburyak.vertx.core.util.Tuple;
import com.dburyak.vertx.core.util.Tuple2;
import com.dburyak.vertx.core.util.Tuple3;
import com.dburyak.vertx.gcp.ProjectIdProvider;
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
import java.util.Map;
import java.util.Random;
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
    private volatile Scheduler elScheduler;
    private volatile Scheduler blockingScheduler;
    private volatile String projectId;
    private volatile JsonObject cachedSecrets;
    private volatile Instant lastRefreshedAt;
    private volatile Disposable gsmUpdNotificationSubscriber;
    private final Collection<Subscription> notificationSubscriptions = synchronizedList(new ArrayList<>());
    private final AtomicInteger subscriptionSuffixCounter = new AtomicInteger();
    private final int rndInstanceId = new Random().nextInt();
    private String hostname = null;
    private final Object hostnameLock = new Object();


    @Override
    public Future<Buffer> get() {
        // avoid extra volatile reads
        var cfgRef = cfg;
        var lastRefreshedAtRef = lastRefreshedAt;

        Single<JsonObject> secretsFuture;
        var resultPromise = Promise.<Buffer>promise();
        if (lastRefreshedAtRef == null || isCacheOutdated(lastRefreshedAtRef, cfgRef)) { // fetch
            secretsFuture = retrieveAndCacheSecrets();
        } else { // use cached
            var cachedSecretsRef = cachedSecrets;
            log.debug("using cached gsm secrets: numSecrets={}, lastRefreshedAt={}",
                    cachedSecretsRef.size(), lastRefreshedAtRef);
            secretsFuture = Single.just(cachedSecretsRef);
        }
        secretsFuture.subscribe(json -> {
            resultPromise.complete(Buffer.buffer(json.encode()));
        }, err -> {
            log.error("gsm secrets retrieval failed: err={}", err.toString());
            resultPromise.fail(err);
        });
        return resultPromise.future();
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
        if (!cfgRef.isPubsubNotificationsEnabled()) {
            return;
        }
        var subscriptionAdminClientRef = subscriptionAdminClient;
        var secretManagerRef = secretManager;
        gsmUpdNotificationSubscriber = Observable.fromIterable(cfgRef.getSecretConfigOptions())
                .filter(opt -> opt.getNotificationTopic() != null && !opt.getNotificationTopic().isBlank())
                .map(cfgOpt -> {
                    var proj = evaluateProjectForOption(projectIdRef, cfgRef, cfgOpt);
                    // secret and topic may be in different projects, but subscriptions will be created/deleted in this
                    // project, so managing subscriptions won't require cross-project "pubsub editor" role
                    var fqnTopic = pubSubUtil.ensureFqnTopic(proj, cfgOpt.getNotificationTopic());
                    return Tuple.of(cfgOpt, proj, fqnTopic);
                })
                .distinct(Tuple3::getV3) // distinct by fqnTopic, we need single subscription per topic
                .flatMapSingle(t -> {
                    var cfgOpt = t.getV1();
                    var proj = t.getV2();
                    var fqnTopic = t.getV3();
                    var fqnSub = pubSubUtil.forceProjectForSubscription(projectIdRef,
                            cfgOpt.getNotificationTopic() + "-" + subscriptionSuffix());
                    return createSubscriptionAndSubscribe(proj, fqnTopic, fqnSub, cfgOpt, subscriptionAdminClientRef,
                            secretManagerRef)
                            .andThen(Single.just(Tuple2.of(cfgOpt, fqnSub)));
                })
                .subscribe(t2 -> log.debug("gsm config updated: cfgOpt={}, secretName={}",
                                t2.getV1().getConfigOption(), t2.getV1().getSecretName()),
                        err -> log.error("gsm config update failed", err));
    }

    @Override
    public Future<Void> close() {
        // TODO: close and delete subscriptions here
        log.debug("closing gsm config store: instance={}", this);
        gsmUpdNotificationSubscriber.dispose();
        var closeFuture = Promise.<Void>promise();
        Flowable.fromIterable(notificationSubscriptions)
                .parallel().runOn(blockingScheduler)
                .flatMap(subscription -> {
                    var subName = subscription.getName();
                    log.debug("deleting gsm config updates subscription: sub={}", subName);
                    try {
                        subscriptionAdminClient.deleteSubscription(subscription.getName());
                        return Flowable.just(subName);
                    } catch (Exception e) {
                        log.error("failed to delete gsm config updates subscription: sub={}", subName, e);
                        return Flowable.error(e);
                    }
                })
                .sequential()
                .subscribe((ignr) -> {}, err -> {}, () -> {
                    log.debug("closed gsm config store: instance={}", this);
                    closeFuture.complete();
                });
        return closeFuture.future();
    }

    private Single<JsonObject> retrieveAndCacheSecrets() {
        return retrieveSecrets().doOnSuccess(json -> {
            cachedSecrets = json;
            lastRefreshedAt = Instant.now();
        });
    }

    private Single<JsonObject> retrieveSecrets() {
        log.debug("retrieving gsm secrets config options");
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
                    var proj = secretOpt.getProjectId();
                    if (proj == null || proj.isBlank()) {
                        proj = cfgRef.getProjectId();
                    }
                    if (proj == null || proj.isBlank()) {
                        proj = projectIdRef;
                    }
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

    private Completable createSubscriptionAndSubscribe(String proj, String fqnTopic, String fqnSub,
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
                    // TODO: check thread hopping carefully for this code and ack
                    // TODO: examine payload, and filter only updates relevant to this secret
                    log.debug("received gsm config update notification: cfgOpt={}, secretName={}",
                            secretOpt.getConfigOption(), secretOpt.getSecretName());
                    return secretManagerRef.getSecretString(proj, secretOpt.getSecretName(), null)
                            .doOnSuccess(secretValue -> cachedSecrets.put(secretOpt.getConfigOption(), secretValue))
                            .ignoreElement()
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
                        hostname = "listener-" + rndInstanceId;
                    }
                }
                podName = hostname;
            }
        }
        var res = podName + "-" + subscriptionSuffixCounter.getAndIncrement();
        log.debug("DELETEME: subscriptionSuffix: {}", res);
        return res;
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
