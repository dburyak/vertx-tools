package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AbstractDiVerticle;
import com.dburyak.vertx.gcp.pubsub.PubSub;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Bean
@Slf4j
public class PubSubVerticle1 extends AbstractDiVerticle {
    private final String topicName = "dmytro-test-1";
    private final String subName = "dmytro-test-1-sub";
    private PubSub pubSub;
    private Disposable subscription;
    private Disposable publisher;

    @Override
    public Completable rxStart() {
        return Completable.fromRunnable(() -> {
            log.debug("subscribing to pubsub: sub={}", subName);
            subscription = pubSub.subscribe(subName)
                    .subscribe(msg -> {
                        log.info("received pubsub msg: sub={}, msg={}", subName, msg.msg());
                        msg.ack().ack();
                    });
            log.debug("publishing to pubsub: topic={}", topicName);
            publisher = Observable.interval(1, TimeUnit.SECONDS)
                    .flatMapSingle(tick -> pubSub.publish(topicName, "hello " + tick + " " + Thread.currentThread()))
                    .subscribe(msgId -> log.info("published pubsub msg: topic={}, msgId={}", topicName, msgId));
        });
    }

    @Override
    public Completable rxStop() {
        return Completable.fromRunnable(() -> {
            log.debug("disposing pubsub verticle for topic: topic={}", topicName);
            publisher.dispose();
            subscription.dispose();
            log.debug("disposed pubsub verticle for topic: topic={}", topicName);
        });
    }

    @Inject
    public void setPubSub(PubSub pubSub) {
        this.pubSub = pubSub;
    }
}
