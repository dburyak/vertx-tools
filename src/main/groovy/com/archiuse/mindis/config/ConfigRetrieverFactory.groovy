package com.archiuse.mindis.config

import com.archiuse.mindis.di.VerticleBean
import io.micronaut.context.annotation.Factory
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.reactivex.config.ConfigRetriever
import io.vertx.reactivex.core.Vertx

import javax.inject.Singleton

@Factory
class ConfigRetrieverFactory {

    @Singleton
    @VerticleBean
    ConfigRetrieverOptions configRetrieverOptions() {
        new ConfigRetrieverOptions().tap {
            includeDefaultStores = true
        }
    }

    @Singleton
    @VerticleBean
    ConfigRetriever configRetriever(Vertx vertx, ConfigRetrieverOptions configRetrieverOptions) {
        ConfigRetriever.create(vertx, configRetrieverOptions)
    }
}
