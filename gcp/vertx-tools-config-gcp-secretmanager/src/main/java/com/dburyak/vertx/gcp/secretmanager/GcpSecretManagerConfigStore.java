package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
import com.dburyak.vertx.gcp.ProjectIdProvider;
import com.dburyak.vertx.gcp.pubsub.PubSub;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ExpirationPolicy;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;
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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

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
    private static final long MSG_RETENTION_DURATION_SECONDS = 10 * 60;
    private static final long SUB_INACTIVITY_EXPIRATION_SECONDS = 24 * 60 * 60;
    private static final int MSG_ACK_DEADLINE_SECONDS = 10;

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
    private volatile Scheduler elScheduler;
    private volatile Scheduler blockingScheduler;
    private volatile String projectId;
    private volatile JsonObject cachedSecrets;
    private volatile Instant lastRefreshedAt;
    private final Collection<Subscription> notificationSubscriptions = synchronizedList(new ArrayList<>());
    private final Collection<Disposable> notificationSubscribers = synchronizedList(new ArrayList<>());


    @Override
    public Future<Buffer> get() {
        // avoid extra volatile reads
        var cfgRef = cfg;
        var lastRefreshedAtRef = lastRefreshedAt;

        Single<JsonObject> secretsFuture;
        var resultPromise = Promise.<Buffer>promise();
        var isRefreshNeeded = lastRefreshedAtRef != null && cfgRef.isRefreshEnabled()
                && Duration.between(lastRefreshedAtRef, Instant.now()).compareTo(cfgRef.getRefreshPeriod()) < 0;
        if (lastRefreshedAtRef == null || isRefreshNeeded) { // fetch
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
        var subscriptionAdminClientRef = subscriptionAdminClient;
        var secretManagerRef = secretManager;

        var pod = podName();
        log.debug("init gsm config store: proj={}, pod={}", projectIdRef, pod);
        if (cfgRef.isPubsubNotificationsEnabled()) {
            for (var secretOpt : cfgRef.getSecretConfigOptions()) {
                var notificationTopic = secretOpt.getNotificationTopic();
                if (notificationTopic != null && !notificationTopic.isBlank()) {
                    var proj = secretOpt.getProjectId();
                    if (proj == null || proj.isBlank()) {
                        proj = cfgRef.getProjectId();
                    }
                    if (proj == null || proj.isBlank()) {
                        proj = projectIdRef;
                    }
                    var projFinal = proj;
                    // secret and topic may be in different project, but subscriptions are created/deleted in this
                    // project, so in order to create/delete it on shutdown we won't require cross-project
                    // "pubsub editor" role
                    // TODO: here .............. check if notificationTopic is already FQN formatted, handle this case
                    var fqnTopic = TopicName.of(proj, notificationTopic);
                    var subName = notificationTopic + "-" + pod;
                    var fqnSub = ProjectSubscriptionName.of(projectIdRef, subName);
                    var secretUpdSubscriber = Single.fromSupplier(() -> {
                                log.debug("create gsm config updates subscription: opt={}, secret={}, topic={}, sub={}",
                                        secretOpt.getConfigOption(), secretOpt.getSecretName(), fqnTopic, fqnSub);
                                return subscriptionAdminClientRef.createSubscription(Subscription.newBuilder()
                                        .setTopic(fqnTopic.toString())
                                        .setName(fqnSub.toString())
                                        .setMessageRetentionDuration(com.google.protobuf.Duration.newBuilder()
                                                .setSeconds(MSG_RETENTION_DURATION_SECONDS))
                                        .setRetainAckedMessages(false)
                                        .setExpirationPolicy(ExpirationPolicy.newBuilder()
                                                .setTtl(com.google.protobuf.Duration.newBuilder()
                                                        .setSeconds(SUB_INACTIVITY_EXPIRATION_SECONDS)))
                                        .setAckDeadlineSeconds(MSG_ACK_DEADLINE_SECONDS)
                                        .build());
                            })
                            .subscribeOn(blockingScheduler) // makes block above to run on worker scheduler
                            .observeOn(elScheduler) // makes all blocks below to run on event loop scheduler
                            .doOnSuccess(subscription -> {
                                log.debug("gsm config updates subscription created: sub={}", subscription.getName());
                                notificationSubscriptions.add(subscription);
                            })
                            .flatMapPublisher(subscription -> {
                                log.debug("subscribing to gsm config updates: sub={}", subscription.getName());
                                return pubSub.subscribe(fqnSub);
                            })
                            .flatMapSingle(upd -> {
                                // TODO: examine payload, and filter only updates relevant to this secret
                                log.debug("received gsm config update notification: cfgOpt={}, secretName={}",
                                        secretOpt.getConfigOption(), secretOpt.getSecretName());
                                return secretManagerRef.getSecretString(projFinal, secretOpt.getSecretName(), null);
                            })
                            .subscribe(secretValue -> {
                                cachedSecrets.put(secretOpt.getConfigOption(), secretValue);
                                log.debug("gsm config updated: cfgOpt={}, secretName={}",
                                        secretOpt.getConfigOption(), secretOpt.getSecretName());
                            }, err -> {
                                log.error("gsm config update failed: sub={}", fqnSub, err);
                            });
                    notificationSubscribers.add(secretUpdSubscriber);
                }
            }

        }
    }

    @Override
    public Future<Void> close() {
        // TODO: close and delete subscriptions here
        log.debug("closing gsm config store: instance={}", this);
        notificationSubscribers.forEach(Disposable::dispose);
        notificationSubscriptions.forEach(subscription -> {
            log.debug("deleting gsm config updates subscription: sub={}", subscription.getName());
            try {
                subscriptionAdminClient.deleteSubscription(subscription.getName());
            } catch (Exception e) {
                log.error("failed to delete gsm config updates subscription: sub={}", subscription.getName(), e);
            }
        });
        return Future.succeededFuture();
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

    private String podName() {
        var podName = System.getenv("POD_NAME");
        if (podName == null || podName.isBlank()) {
            podName = System.getProperty("pod.name");
        }
        if (podName == null || podName.isBlank()) {
            podName = "listener-" + new Random().nextInt(1000);
        }
        return podName;
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
