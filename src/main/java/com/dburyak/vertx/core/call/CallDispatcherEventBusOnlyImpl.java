package com.dburyak.vertx.core.call;

import io.micronaut.context.annotation.Secondary;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;


// TODO: should probably be a global bean, or at least part that configures routes and deploys http verticles should

/**
 * This implementation doesn't support routing to/from http verticles, only event bus messages.
 */
@Singleton
@Secondary
@Slf4j
public class CallDispatcherEventBusOnlyImpl implements CallDispatcher {
    private RoutingConfig routing;
    private Map<String, String> ebAddr;

    @Override
    public Completable notify(@NotNull Request request) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable notify(@NotNull Map<String, Object> requestParams) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable notify(@NotBlank String action, Object args, Object msg, DeliveryOptions opts) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable notify(@NotBlank String action, Object args, Object msg, Map<String, Object> headers) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(@NotNull Request request) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(@NotNull Map<String, Object> requestParams) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(@NotBlank String action, Object args, Object msg, DeliveryOptions opts) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Response> request(@NotBlank String action, Object args, Object msg, Map<String, Object> headers) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(@NotNull Request request) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(@NotNull Map<String, Object> requestParams) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(@NotBlank String action, Object args, Object msg, DeliveryOptions opts) {
        // TODO: implement
        return null;
    }

    @Override
    public Completable publish(@NotBlank String action, Object args, Object msg, Map<String, Object> headers) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Disposable> onNotification(@NotBlank String action, @NotNull Handler<Request> doOnNotification) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Disposable> onRequest(@NotBlank String action, @NotNull Function<Request, Single<Response>> doOnRequest) {
        // TODO: implement
        return null;
    }

    @Override
    public Single<Disposable> subscribe(@NotBlank String action, @NotNull Handler<Request> doOnEvent) {
        // TODO: implement
        return null;
    }

    @Inject
    @Override
    public void setRouting(@NotNull RoutingConfig routing) {
        this.routing = routing;
    }
}
