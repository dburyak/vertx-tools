package com.dburyak.vertx.eventbus.config;

import com.dburyak.vertx.core.VertxOptionsConfigurer;
import com.dburyak.vertx.eventbus.EventBusConfigurer;
import com.dburyak.vertx.eventbus.NamedMessageCodec;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.EventBus;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Factory for default {@link EventBus} related beans. It is strongly recommended to not override any of these beans,
 * and instead use {@link EventBusConfigurer} to customize {@link EventBus} instance.
 */
@Factory
@Slf4j
public class EventBusFactory {

    /**
     * Default {@link io.vertx.core.eventbus.EventBus} bean. It is strongly recommended to not override this bean, and
     * instead use {@link EventBusConfigurer} to customize {@link EventBus} instance.
     *
     * @param appCtx micronaut application context
     * @param vertx vertx instance
     * @param eventBusProperties event bus properties
     *
     * @return event bus bean
     */
    @Context
    @Requires(missingBeans = io.vertx.core.eventbus.EventBus.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public io.vertx.core.eventbus.EventBus coreEventBus(ApplicationContext appCtx, Vertx vertx,
            EventBusProperties eventBusProperties) {
        var eb = vertx.getDelegate().eventBus();
        eventBusProperties.getCodecs().forEach(codecProps -> {
            if (!codecProps.isEnabled()) {
                return;
            }
            try {
                var codecType = Class.forName(codecProps.getType());
                var codec = (MessageCodec) appCtx.getBean(codecType);
                if (!codecProps.isDefault()) {
                    var namedCodec = NamedMessageCodec.of(codecProps.getName(), codec);
                    log.info("register eb codec: codec={}", namedCodec);
                    eb.registerCodec(namedCodec);
                } else if (codecProps.getDefaultTypes().size() == 1) {
                    try {
                        var defaultType = Class.forName(codecProps.getDefaultTypes().get(0));
                        var namedCodec = NamedMessageCodec.of(codecProps.getName(), codec);
                        log.info("register eb default codec: forType={}, codec={}", defaultType, namedCodec);
                        eb.registerDefaultCodec(defaultType, namedCodec);
                    } catch (ClassNotFoundException e) {
                        var errMsg = "failed to find target class for default codec: codecProps={}";
                        if (eventBusProperties.shouldFailOnCodecError()) {
                            log.error(errMsg, codecProps);
                            throw new RuntimeException(e);
                        } else {
                            log.warn(errMsg, codecProps);
                        }
                    }
                } else {
                    for (int i = 0; i < codecProps.getDefaultTypes().size(); i++) {
                        try {
                            var defaultTypeStr = codecProps.getDefaultTypes().get(i);
                            var defaultType = Class.forName(defaultTypeStr);
                            var namedCodec = NamedMessageCodec.of(codecProps.getName() + "-" + defaultTypeStr, codec);
                            log.info("register eb default codec: forType={}, codec={}", defaultType, namedCodec);
                            eb.registerDefaultCodec(defaultType, namedCodec);
                        } catch (ClassNotFoundException e) {
                            var errMsg = "failed to find target class for default codec: codecProps={}, codecNum={}";
                            if (eventBusProperties.shouldFailOnCodecError()) {
                                log.error(errMsg, codecProps, i);
                                throw new RuntimeException(e);
                            } else {
                                log.warn(errMsg, codecProps, i);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                var errMsg = "failed to find codec class: codecProps={}";
                if (eventBusProperties.shouldFailOnCodecError()) {
                    log.error(errMsg, codecProps);
                    throw new RuntimeException(e);
                } else {
                    log.warn(errMsg, codecProps);
                }
            }
        });
        return eb;
    }

    /**
     * Default rx-fied {@link EventBus} bean.
     *
     * @param coreEventBus core plain vertx event bus
     *
     * @return rx event bus bean
     */
    @Singleton
    @Requires(missingBeans = EventBus.class)
    public EventBus rxEventBus(io.vertx.core.eventbus.EventBus coreEventBus) {
        // "rx.EventBus" is NOT thread safe, but the wrapped core.EventBus is,
        // so here we eagerly initialize core.EventBus delegate and avoid not-thread-safe caching in rx.EventBus
        // since produced object will never change, and creation will happen before other threads will
        // inject reference to created object, safe publishing is guaranteed here

        return EventBus.newInstance(coreEventBus);
    }

    /**
     * {@link VertxOptionsConfigurer} that applies all registered {@link EventBusConfigurer}s and makes this config
     * being applied to vertx.
     *
     * @param eventBusConfigurers all registered event bus configurers
     *
     * @return vertx options configurer for applying event bus configurers
     */
    @Singleton
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
