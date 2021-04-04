package com.dburyak.vertx.core.eventbus;

import io.micronaut.context.annotation.Secondary;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageProducer;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Secondary
public class CallDispatcherDefaultImpl implements CallDispatcher {

    @Setter(onMethod_ = {@Inject})
    private EventBus eventBus;


    @Override
    public Completable send(String action, Object msg, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <T> MessageProducer<T> sender(String action, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <R> Single<Message<R>> request(String action, Object msg, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public Completable publish(String action, Object msg, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <T> MessageProducer<T> publisher(String action, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <T> Single<Disposable> consumer(String action, Handler<Message<T>> doOnCall) {
        return null;
    }

    @Override
    public <T> Single<Disposable> localConsumer(String action, Handler<Message<T>> doOnLocalCall) {
        return null;
    }
}
