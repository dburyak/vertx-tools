package com.dburyak.vertx.gcp.pubsub;

import com.dburyak.vertx.core.executor.VertxCtxMinimalStrictScheduledExecutorService;
import com.google.cloud.pubsub.v1.AckReplyConsumerWithResponse;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PubSubDelivery implements Delivery {
    private final AckReplyConsumerWithResponse ackDelegate;
//    private final Vertx vertx;
    
    @Override
    public Completable ack() {
        log.debug("ack() called: obj={}", this);
        // TODO: make sure that ack/nack is executed on the same thread as the caller of this method
        return Completable.create(emitter -> {
            var future = ackDelegate.ack();
            future.addListener(() -> {
                log.debug("ack future completed: obj={}", this);
                if (future.isDone()) {
                    if (!future.isCancelled()) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IllegalStateException("underlying ack future was cancelled"));
                    }
                } else {
                    emitter.onError(new IllegalStateException("underlying ack future was not completed"));
                }
            }, Runnable::run);
        });
    }

    @Override
    public Completable nack() {
        return Completable.create(emitter -> {
            var future = ackDelegate.nack();
            future.addListener(() -> {
                if (future.isDone()) {
                    if (!future.isCancelled()) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IllegalStateException("underlying nack future was cancelled"));
                    }
                } else {
                    emitter.onError(new IllegalStateException("underlying nack future was not completed"));
                }
            }, Runnable::run);
        });
    }
}
