package com.dburyak.vertx.discovery;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;

import javax.inject.Singleton;

@Factory
@Secondary
public class ServiceDiscoveryFactory {

    @Singleton
    @Bean(preDestroy = "close")
    @Secondary
    public ServiceDiscovery serviceDiscovery(Vertx vertx) {
        return ServiceDiscovery.create(vertx);
    }
}
