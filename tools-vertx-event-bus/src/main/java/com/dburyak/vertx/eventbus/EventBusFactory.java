package com.dburyak.vertx.eventbus;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Factory
@Secondary
@Slf4j
public class EventBusFactory {

    @Singleton
    @Secondary
    public EventBus eventBus(Vertx vertx, MessageCodec<Object, Object> ebMsgCodec) {
        // rx.EventBus is not thread safe, but the wrapped core.EventBus is thread safe,
        // so per-verticle singleton thin wrapper should be used that wraps single-in-app thread safe instance

        var eb = EventBus.newInstance(vertx.getDelegate().eventBus());
        try {
            eb.registerCodec(ebMsgCodec);
            log.debug("registered EB codec: {}", ebMsgCodec.name());
        } catch (Exception ignored) {
            // codec is already registered by another verticle
            log.debug("avoid duplicate EB codec registration");
        }
        return eb;
    }
}
