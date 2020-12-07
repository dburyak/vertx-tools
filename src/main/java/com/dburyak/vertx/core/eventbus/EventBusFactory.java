package com.dburyak.vertx.core.eventbus;

import io.micronaut.context.annotation.Factory;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Factory
@Setter(onMethod_ = {@Inject})
@Slf4j
public class EventBusFactory {
    private MessageCodec<Object, Object> ebMsgCodec;

    @Singleton
    public EventBus eventBus(Vertx vertx) {
        // rx.EventBus is not thread safe, but the wrapped core.EventBus is thread safe,
        // so per-verticle singleton thin wrapper should be used that wraps single-in-app thread safe instance

        var eb = EventBus.newInstance(vertx.getDelegate().eventBus());
        try {
            eb.registerCodec(ebMsgCodec);
            log.debug("registered EB codec: {}", ebMsgCodec.name());
        } catch (Exception ignored) {
            // codec is already registered by another instance of call dispatcher
            log.debug("avoid duplicate EB codec registration");
        }
        return eb;
    }
}
