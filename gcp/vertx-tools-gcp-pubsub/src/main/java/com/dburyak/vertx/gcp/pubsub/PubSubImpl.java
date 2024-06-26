package com.dburyak.vertx.gcp.pubsub;

import com.dburyak.vertx.core.executor.VertxCtxMinimalStrictScheduledExecutorService;
import com.dburyak.vertx.gcp.ProjectIdProvider;
import com.dburyak.vertx.gcp.pubsub.config.PubSubProperties;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.MessageReceiverWithAckResponse;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.annotation.Requires;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.vertx.rxjava3.core.Vertx;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Singleton
@Requires(missingBeans = PubSub.class)
@Slf4j
public class PubSubImpl implements PubSub {

    private final Vertx vertx;
    private final PubSubProperties cfg;
    private final String projectId;
    private final PubSubUtil pubSubUtil;

    private final ConcurrentMap<String, Publisher> publishers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Thread, ScheduledExecutorService> vertxCtxExecutors = new ConcurrentHashMap<>();
    private final List<Subscriber> subscribers = synchronizedList(new ArrayList<>());
    private final ConcurrentMap<String, String> canonicalTopics = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> canonicalSubscriptions = new ConcurrentHashMap<>();

    public PubSubImpl(Vertx vertx, PubSubProperties cfg, ProjectIdProvider projectIdProvider, PubSubUtil pubSubUtil) {
        this.vertx = vertx;
        this.cfg = cfg;
        this.projectId = projectIdProvider.getProjectId();
        this.pubSubUtil = pubSubUtil;
    }

    @Override
    public Single<String> publish(String topic, PubsubMessage msg) {
        return getPublisherFor(fqnTopic(topic))
                .flatMap(publisher -> toSingle(publisher.publish(msg)));
    }

    @Override
    public Single<String> publish(String topic, String msg) {
        return Single.fromSupplier(() -> toPubsubMessage(msg, null))
                .flatMap(pubsubMsg -> publish(fqnTopic(topic), pubsubMsg));
    }

    @Override
    public Single<String> publish(String topic, String msg, Map<String, String> attributes) {
        return Single.fromSupplier(() -> toPubsubMessage(msg, attributes))
                .flatMap(pubsubMsg -> publish(fqnTopic(topic), pubsubMsg));
    }

    @Override
    public Single<String> publish(String topic, byte[] msg) {
        return Single.fromSupplier(() -> toPubsubMessage(msg, null))
                .flatMap(pubsubMsg -> publish(fqnTopic(topic), pubsubMsg));
    }

    @Override
    public Single<String> publish(String topic, byte[] msg, Map<String, String> attributes) {
        return Single.fromSupplier(() -> toPubsubMessage(msg, attributes))
                .flatMap(pubsubMsg -> publish(fqnTopic(topic), pubsubMsg));
    }

    @Override
    public Observable<DeliverableMsg> subscribe(String subscription) {
        var vertxCtxExecutor = currentVertxCtxExecutor();
        var subscriberRef = new AtomicReference<Subscriber>();
        return Observable.<DeliverableMsg>create(emitter -> {
                    MessageReceiverWithAckResponse msgReceiver = (msg, ack) -> vertxCtxExecutor.execute(() -> {
                        if (emitter.isDisposed()) {
                            ack.nack();
                        } else {
                            emitter.onNext(new DeliverableMsg(msg, new PubSubDelivery(ack)));
                        }
                    });
                    var subscriber = Subscriber.newBuilder(fqnSub(subscription), msgReceiver)
                            .setExecutorProvider(new FixedExecutorProvider(vertxCtxExecutor))
                            // TODO: provide advanced config (batching, retry, etc)
                            .build();
                    subscriberRef.set(subscriber);
                    subscriber.startAsync();
                    subscribers.add(subscriber);
                    emitter.setCancellable(() -> {
                        log.debug("pubsub subscriber observable cancelled: sub={}", subscription);
                        stopSubscriber(subscriberRef);
                    });
                })
                .doOnSubscribe(ignr -> log.debug("subscribing to pubsub: sub={}", subscription))
                .doOnTerminate(() -> {
                    log.debug("pubsub subscriber observable terminated: sub={}", subscription);
                    stopSubscriber(subscriberRef);
                });
    }

    @PreDestroy
    public void destroy() {
        // TODO: parallelize waiting for shutdown

        var shutdownStartedAt = Instant.now();
        log.debug("closing pubsub: publishers={}, subscribers={}", publishers.size(), subscribers.size());
        var failures = new ArrayList<Exception>();
        subscribers.forEach(Subscriber::stopAsync);
        var subscriberShutdownTimeoutMs = cfg.getSubscriberProperties().getShutdownTimeout().toMillis();
        for (var subscriber : subscribers) {
            try {
                subscriber.awaitTerminated(subscriberShutdownTimeoutMs, MILLISECONDS);
            } catch (Exception e) {
                failures.add(e);
                log.error("subscriber failed to shutdown in time: sub={}", subscriber.getSubscriptionNameString());
            }
        }
        subscribers.clear();

        publishers.values().forEach(Publisher::shutdown);
        var publisherShutdownTimeoutMs = cfg.getPublisherProperties().getShutdownTimeout().toMillis();
        for (var publisher : publishers.values()) {
            try {
                publisher.publishAllOutstanding();
                publisher.awaitTermination(publisherShutdownTimeoutMs, MILLISECONDS);
            } catch (Exception e) {
                failures.add(e);
            }
        }
        publishers.clear();
        vertxCtxExecutors.clear();
        var shutdownTime = Duration.between(shutdownStartedAt, Instant.now());
        log.debug("closed pubsub: shutdownTime={}, failures={}", shutdownTime, failures.size());
        if (!failures.isEmpty()) {
            // using CompositeException from rxjava3, as we use rxjava3 everywhere already
            throw new CompositeException(failures);
        }
    }

    private void stopSubscriber(AtomicReference<Subscriber> subscriberRef) throws TimeoutException {
        var subscriber = subscriberRef.getAndSet(null);
        if (subscriber != null) {
            var subName = subscriber.getSubscriptionNameString();
            var shutdownStartedAt = Instant.now();
            log.debug("stopping pubsub subscriber: sub={}", subName);
            subscriber.stopAsync();

            // TODO: don't wait here for too long, if subscriber has dangling messages, it's safe to just ignore it
            // https://github.com/googleapis/google-cloud-java/issues/3752

            subscriber.awaitTerminated(30, SECONDS); // TODO: make timeout configurable
            var shutdownDuration = Duration.between(shutdownStartedAt, Instant.now());
            log.debug("stopped pubsub subscriber: sub={}, shutdownDuration={}", subName, shutdownDuration);
        }
    }

    private Single<Publisher> getPublisherFor(String topic) {
        return Single.fromSupplier(() -> publishers.computeIfAbsent(topic, t -> {
            try {
                return Publisher.newBuilder(t)
                        // TODO: provide advanced config (batching, retry, etc)
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private Single<String> toSingle(ApiFuture<String> future) {
        var vertxCtxExecutor = currentVertxCtxExecutor();
        return Single.create(emitter -> future.addListener(() -> {
            try {
                emitter.onSuccess(future.get());
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, vertxCtxExecutor));
    }

    private PubsubMessage toPubsubMessage(String msg, Map<String, String> attributes) {
        var builder = PubsubMessage.newBuilder()
                .setData(ByteString.copyFrom(msg, UTF_8));
        if (attributes != null) {
            builder.putAllAttributes(attributes);
        }
        return builder.build();
    }

    private PubsubMessage toPubsubMessage(byte[] msg, Map<String, String> attributes) {
        var builder = PubsubMessage.newBuilder()
                .setData(ByteString.copyFrom(msg));
        if (attributes != null) {
            builder.putAllAttributes(attributes);
        }
        return builder.build();
    }

    private ScheduledExecutorService currentVertxCtxExecutor() {
        return vertxCtxExecutors.computeIfAbsent(Thread.currentThread(),
                t -> new VertxCtxMinimalStrictScheduledExecutorService(vertx));
    }

    private String fqnTopic(String topic) {
        if (pubSubUtil.isFqn(topic)) {
            return topic;
        }
        return canonicalTopics.computeIfAbsent(topic, t -> pubSubUtil.fqnTopic(projectId, t));
    }

    private String fqnSub(String subscription) {
        if (pubSubUtil.isFqn(subscription)) {
            return subscription;
        }
        return canonicalSubscriptions.computeIfAbsent(subscription, s -> pubSubUtil.fqnSubscription(projectId, s));
    }
}
