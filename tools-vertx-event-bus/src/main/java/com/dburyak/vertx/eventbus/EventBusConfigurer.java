package com.dburyak.vertx.eventbus;

import io.vertx.core.eventbus.EventBusOptions;

public interface EventBusConfigurer {
    EventBusOptions configure(EventBusOptions options);
}
