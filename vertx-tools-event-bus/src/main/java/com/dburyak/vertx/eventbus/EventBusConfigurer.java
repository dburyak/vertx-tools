package com.dburyak.vertx.eventbus;

import io.vertx.core.eventbus.EventBusOptions;

/**
 * EventBus configurer is a function that takes an EventBusOptions object and returns a modified EventBusOptions.
 * It can return both the same object or a new object.
 */
public interface EventBusConfigurer {

    /**
     * Configure EventBusOptions.
     *
     * @param options EventBusOptions to configure
     *
     * @return configured EventBusOptions
     */
    EventBusOptions configure(EventBusOptions options);
}
