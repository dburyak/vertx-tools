package com.archiuse.mindis.call

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.servicediscovery.ServiceDiscovery

import javax.inject.Singleton

@Factory
class ServiceDiscoveryFactory {

    @Singleton
    @Bean(preDestroy = 'close')
    ServiceDiscovery serviceDiscovery(Vertx vertx) {
        ServiceDiscovery.create(vertx)
    }
}
