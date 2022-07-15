package com.dburyak.vertx.eventbus;

import com.dburyak.vertx.core.VertxOptionsConfigurer;
import com.dburyak.vertx.eventbus.config.EventBusProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.eventbus.EventBusOptions;
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

    @Context
    @Secondary
    public io.vertx.core.eventbus.EventBus coreEventBus(Vertx vertx, EventBusProperties eventBusProperties,
            List<MessageCodec<?, ?>> ebMsgCodecs) {
        var eb = vertx.getDelegate().eventBus();
        // TODO: currently here .............. register codecs based on config properties object
//        eventBusProperties.getCodecs().forEach((name, codecCfg) -> {
//            if (!codecCfg.isEnabled()) {
//                return;
//            }
//            try {
//                var codecType = Class.forName(codecCfg.getType());
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        });

        // FIXME: ...... ^^^^^
        ebMsgCodecs.forEach(codec -> {
            log.debug("register EB codec: {}", codec.name());
            eb.registerCodec(codec);
        });
        return eb;
    }

    @Singleton
    @Secondary
    public EventBus rxEventBus(io.vertx.core.eventbus.EventBus coreEventBus) {
        // rx.EventBus is NOT thread safe, but the wrapped core.EventBus is;
        // so here we eagerly initialize core.EventBus delegate and avoid not-thread-safe caching in rx.EventBus
        // since produced object will never change, and creation will happen before other threads will
        // inject reference to created object, safe publishing is guaranteed here

        return EventBus.newInstance(coreEventBus);
    }

    @Singleton
    @Secondary
    public VertxOptionsConfigurer eventBusConfigurersApplied(List<EventBusConfigurer> eventBusConfigurers) {
        return (vertxOptions) -> {
            var ebOptions = new EventBusOptions();
            for (var eventBusConfigurer : eventBusConfigurers) {
                ebOptions = eventBusConfigurer.configure(ebOptions);
            }
            return vertxOptions.setEventBusOptions(ebOptions);
        };
    }
}
