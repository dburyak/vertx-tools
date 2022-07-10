package com.dburyak.vertx.eventbus;

import com.dburyak.vertx.core.di.VertxThreadScope;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.EventBus;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Factory
@Secondary
@Slf4j
public class EventBusFactory {

    @Singleton
    @Secondary
    public io.vertx.core.eventbus.EventBus coreEventBus(Vertx vertx, List<MessageCodec<?, ?>> ebMsgCodecs) {
        var eb = vertx.getDelegate().eventBus();
        ebMsgCodecs.forEach(codec -> {
            log.debug("register EB codec: {}", codec.name());
            eb.registerCodec(codec);
        });
        return eb;
    }

    @Bean
    @VertxThreadScope
    @Secondary
    public EventBus rxEventBus(io.vertx.core.eventbus.EventBus coreEventBus) {
        // rx.EventBus is NOT thread safe, but the wrapped core.EventBus is thread safe,
        // so per-thread singleton thin wrapper should be used that wraps single-in-app thread safe instance

        return EventBus.newInstance(coreEventBus);
    }
}
