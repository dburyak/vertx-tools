package com.archiuse.mindis.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.reactivex.config.ConfigRetriever
import io.vertx.reactivex.core.Vertx

import javax.inject.Singleton

@Factory
class ConfigRetrieverFactory {

    @Singleton
    ConfigRetrieverOptions configRetrieverOptions() {
        new ConfigRetrieverOptions().tap {
            includeDefaultStores = true
        }
    }

    @Singleton
    @Bean(preDestroy = 'close')
    ConfigRetriever configRetriever(Vertx vertx, ConfigRetrieverOptions configRetrieverOptions) {
        ConfigRetriever.create(vertx, configRetrieverOptions)
    }
}
