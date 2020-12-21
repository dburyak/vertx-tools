package com.dburyak.vertx.core.call;

import io.micronaut.context.annotation.Secondary;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Map;


// TODO: should probably be a global bean, or at least part that configures routes and deploys http verticles should
@Singleton
@Secondary
@Slf4j
public class CallDispatcherEventBusOnlyImpl implements CallDispatcher {

    @Override
    public Completable notify(Request request) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable notify(Map<String, Object> requestParams) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable notify(String action, Object args, Object msg, DeliveryOptions opts) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable notify(String action, Object args, Object msg, Map<String, Object> headers) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(Request request) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(Map<String, Object> requestParams) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(String action, Object args, Object msg, DeliveryOptions opts) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(String action, Object args, Object msg, Map<String, Object> headers) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(Request request) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(Map<String, Object> requestParams) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(String action, Object args, Object msg, DeliveryOptions opts) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(String action, Object args, Object msg, Map<String, Object> headers) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Disposable> onNotification(String action, Handler<Request> doOnNotification) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Disposable> onRequest(String action, Function<Request, Single<Response>> doOnRequest) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Disposable> subscribe(String action, Handler<Request> doOnEvent) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable configure(RoutingConfig routing) {
        // TODO: implement
        return null;
    }
}
