package com.archiuse.mindis

import com.archiuse.mindis.call.CallReceiverDescription
import com.archiuse.mindis.call.ServiceHelper
import com.archiuse.mindis.di.VerticleBean
import com.archiuse.mindis.di.Vertx
import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Primary
import io.micronaut.inject.qualifiers.Qualifiers
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Context

import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@Vertx
@Slf4j
abstract class MindisVerticle extends AbstractVerticle implements CallReceiverDescription {
    private volatile Context vertxContext
    protected volatile ApplicationContext verticleBeanCtx

    @Override
    final Completable rxStart() {
        vertxContext = new Context(super.@context)
        Completable
                .fromAction {
                    log.info 'starting verticle: {}', this
                    log.info 'starting verticle bean context: verticle={}, verticleCtx={}', this, verticleBeanCtx
                    verticleBeanCtx.registerSingleton(ApplicationContext, verticleBeanCtx, Qualifiers.byQualifiers(
                            Qualifiers.byStereotype(Primary), Qualifiers.byStereotype(VerticleBean)))
                    verticleBeanCtx.registerSingleton(this)
                    verticleBeanCtx.refreshBean(verticleBeanCtx.findBeanRegistration(verticleBeanCtx).get().identifier)
                    verticleBeanCtx.refreshBean(verticleBeanCtx.findBeanRegistration(this).get().identifier)
                }
                .andThen(Completable.defer { doStart() })
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

    Context getVertxContext() {
        vertxContext
    }

    @PostConstruct
    protected void init(ServiceHelper serviceHelper) {
        if (!receiverName) {
            receiverName = this.class.canonicalName
        }
        actions << serviceHelper.healthAction
        actions << serviceHelper.readinessAction
    }

    protected Completable doStart() {
        Completable.complete()
    }

    protected Completable doStop() {
        Completable.complete()
    }
}
