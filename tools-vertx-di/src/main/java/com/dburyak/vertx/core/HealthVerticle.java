package com.dburyak.vertx.core;

import io.reactivex.Completable;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.healthchecks.HealthChecks;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;

// TODO: review eb addresses manipulation; this should be updated to work with CallDispatcher/CommunicationsBuilder
@Singleton
@Setter(onMethod_ = {@Inject})
public abstract class HealthVerticle extends Verticle {
    private HealthChecks healthChecks;
    private HealthChecks readyChecks;
    private EventBus eventBus;
    private MessageCodec<Object, Object> ebMsgCodec;

    @Override
    protected final Completable doOnStart() {
        return Completable
                .fromAction(() -> {
                    registerHealthProcedures(healthChecks);
                    registerReadyProcedures(readyChecks);
                })
                .andThen(Completable.fromAction(() -> {
                    eventBus.consumer(getHealthAddr(), msg -> healthChecks.invoke(json ->
                            msg.reply(json.getMap(), new DeliveryOptions().setCodecName(ebMsgCodec.name()))));
                    eventBus.consumer(getReadyAddr(), msg -> readyChecks.invoke(json ->
                            msg.reply(json.getMap(), new DeliveryOptions().setCodecName(ebMsgCodec.name()))));
                }));
    }

    public String getHealthAddr() {
        return getClass() + ".health";
    }

    public String getReadyAddr() {
        return getClass() + ".ready";
    }

    protected void registerHealthProcedures(HealthChecks healthChecks) {
    }

    protected void registerReadyProcedures(HealthChecks readyChecks) {
    }
}
