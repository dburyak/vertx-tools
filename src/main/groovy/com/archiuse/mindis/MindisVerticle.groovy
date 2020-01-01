package com.archiuse.mindis

import com.archiuse.mindis.di.Vertx
import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle

import javax.inject.Singleton

@Singleton
@Vertx
@Slf4j
abstract class MindisVerticle extends AbstractVerticle {
    @Override
    final Completable rxStart() {
        doStart()
                .doOnSubscribe { log.info 'starting verticle: {}', this }
                .doOnComplete { log.info 'verticle started: {}', this }
    }

    @Override
    final Completable rxStop() {
        doStop()
                .doOnSubscribe { log.info 'stopping verticle: {}', this }
                .doOnComplete { log.info 'verticle stopped: {}', this }
    }

    protected Completable doStart() {
        Completable.complete()
    }

    protected Completable doStop() {
        Completable.complete()
    }
}
