package com.dburyak.vertx.test;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.vertx.core.VertxOptions;

@Factory
public class VertxOptsFactory {

    @Bean
    public VertxOptions vertxOptions() {
        return new VertxOptions().setEventLoopPoolSize(2);
    }
}
