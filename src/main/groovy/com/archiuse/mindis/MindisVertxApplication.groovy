package com.archiuse.mindis

import com.archiuse.mindis.di.AppBean
import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.context.Qualifier
import io.micronaut.inject.qualifiers.Qualifiers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.reactivex.core.Vertx
import org.slf4j.Logger

import java.util.concurrent.ConcurrentHashMap

/**
 * Mindis vertx application carcass.
 * Defines initialization of main ApplicationContext and isolated per-verticle ApplicationContexts.
 */
@Slf4j
abstract class MindisVertxApplication {
    static final PROP_IS_APP_BEAN_CTX = 'vertx.app.bean.ctx.main'

    volatile ApplicationContext applicationBeanContext
    final Map<String, ApplicationContext> verticlesBeanContexts = new ConcurrentHashMap<>()

    final Completable start() {
        Completable
                .fromAction {
                    def isRunning = applicationBeanContext as Boolean
                    if (isRunning) {
                        throw new MindisException('application is already running')
                    }
                }

                .andThen(startAppContext())
                .doOnSubscribe { log.info 'starting application' }
                .doOnSuccess { applicationBeanContext = it }

                .flatMapCompletable { ApplicationContext mainCtx ->
                    def vertx = mainCtx.getBean Vertx
                    Observable
                            .fromIterable(verticlesProducers)
                            .flatMapCompletable { VerticleProducer verticleSupplier ->
                                log.debug 'init bean context for verticle'
                                def verticleCtx = ApplicationContext.build()
                                        .properties((PROP_IS_APP_BEAN_CTX): false)
                                        .start()

                                log.debug 'inject main app beans into verticle bean context: ' +
                                        'mainCtx={}, verticleCtx={}', mainCtx, verticleCtx
                                injectMainBeansToVerticleBeanContext verticleCtx, mainCtx

                                log.debug 'deploying verticle: verticleCtx={}', verticleCtx
                                verticleSupplier.verticleBeanCtx = verticleCtx
                                vertx.rxDeployVerticle(verticleSupplier.verticleSupplier,
                                        verticleSupplier.deploymentOptions)
                                        .doOnSuccess {
                                            log.debug 'verticle deployed: depId={}, verticleCtx={}',
                                                    it, verticleCtx
                                            verticlesBeanContexts[it] = verticleCtx
                                        }
                                        .ignoreElement()
                            }
                }
                .doOnComplete { log.info 'application started' }
                .doOnError { log.error 'failed to start application', it }
    }

    final Completable stop() {
        Single
                .fromCallable {
                    def mainCtx = applicationBeanContext
                    def isRunning = mainCtx as Boolean
                    if (!isRunning) {
                        throw new MindisException('application is already stopped')
                    }
                    mainCtx
                }

        // stop vertx
                .flatMap { ApplicationContext mainCtx ->
                    mainCtx.getBean(Vertx)
                            .rxClose()
                            .doOnSubscribe { log.info 'stopping application' }
                            .doOnSubscribe { log.debug 'closing vertx' }
                            .doOnComplete { log.debug 'vertx closed' }
                            .toSingleDefault(mainCtx)
                }

        // stop bean context for each verticle
                .flatMapObservable { ApplicationContext mainCtx ->
                    Observable
                            .fromIterable(verticlesBeanContexts.entrySet())
                            .doOnSubscribe { log.debug 'stopping all verticle bean contexts' }
                            .doOnNext {
                                def deploymentId = it.key
                                def verticleCtx = it.value
                                log.debug 'closing verticle bean context: depId={}, verticleCtx={}',
                                        deploymentId, verticleCtx
                                verticleCtx.stop()
                                verticlesBeanContexts.remove(deploymentId)
                                log.debug 'verticle bean context closed: depId={}, verticleCtx={}',
                                        deploymentId, verticleCtx
                            }
                            .doOnComplete { log.debug 'all verticle bean contexts stopped' }

                    // stop main application bean context
                            .concatWith(Completable.fromAction {
                                log.debug 'stopping application main bean context: appCtx={}', mainCtx
                                mainCtx.stop()
                                applicationBeanContext = null
                                log.debug 'application main bean context stopped: appCtx={}', mainCtx
                            })
                }
                .ignoreElements()
                .doOnComplete { log.info 'application stopped' }
                .doOnError { log.error 'failed to stop application', it }
    }

    abstract List<VerticleProducer> getVerticlesProducers()

    private Single<ApplicationContext> startAppContext() {
        Single
                .<ApplicationContext> fromCallable {
                    log.debug 'starting main bean context'
                    def appCtx = ApplicationContext.build()
                            .properties((PROP_IS_APP_BEAN_CTX): true)
                            .start()
                    log.debug 'main bean context started'
                    appCtx
                }
    }

    protected void injectMainBeansToVerticleBeanContext(ApplicationContext verticleCtx, ApplicationContext mainCtx) {
        [[ApplicationContext, mainCtx, Qualifiers.byStereotype(AppBean)],
         [Vertx, mainCtx.getBean(Vertx), null]
        ].each { Class beanType, def bean, Qualifier qualifier ->
            log.debug 'inject app bean: type={}, bean={}, verticleCtx={}',
                    beanType, bean, verticleCtx
            if (qualifier) {
                verticleCtx.registerSingleton(beanType, bean, qualifier)
            } else {
                verticleCtx.registerSingleton(beanType, bean)
            }
        }
    }

    protected Logger getLog() {
        log
    }
}
