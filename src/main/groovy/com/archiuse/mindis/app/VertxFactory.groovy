package com.archiuse.mindis.app

import com.archiuse.mindis.di.AppBean
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Factory
import io.vertx.reactivex.core.Vertx

import javax.inject.Singleton

@Slf4j
@AppBean
@Factory
class VertxFactory {

    @Singleton
    @AppBean
    Vertx vertx() {
        def vertx = Vertx.vertx()
        log.info 'create vertx : {}', vertx
        vertx
    }
}
