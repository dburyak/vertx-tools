package com.archiuse.mindis

import com.archiuse.mindis.call.CallReceiverDescription
import com.archiuse.mindis.di.Vertx
import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle

import javax.inject.Singleton

@Singleton
@Vertx
@Slf4j
abstract class MindisVerticle extends AbstractVerticle implements CallReceiverDescription {

    MindisVerticle() {
        receiverAddress = this.class.canonicalName
    }

    @Override
    final Completable rxStart() {
        Completable
                .fromAction { log.info 'starting verticle: {}', this }
                .andThen(doStart())
                .doOnComplete { log.info 'verticle started: {}', this }
                .doOnError { log.error 'failed to start verticle: {}', this, it }
    }

    @Override
    final Completable rxStop() {
        Completable
                .fromAction { log.info 'stopping verticle: {}', this }
                .andThen(doStop())
                .doOnComplete { log.info 'verticle stopped: {}', this }
                .doOnError { log.error 'failed to stop verticle: {}', this, it }
    }

    protected Completable doStart() {
        Completable.complete()
    }

    protected Completable doStop() {
        Completable.complete()
    }
}
