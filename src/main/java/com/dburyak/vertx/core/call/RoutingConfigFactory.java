package com.dburyak.vertx.core.call;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;

@Factory
@Secondary
public class RoutingConfigFactory {

    @Bean
    @Secondary
    public Routing routingConfig() {
        // TODO: implement parsing of routing config
        return new Routing();
    }
}
