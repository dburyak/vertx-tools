package com.archiuse.mindis

import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.reactivex.core.Vertx
import org.slf4j.Logger

import java.util.concurrent.ConcurrentHashMap

/**
 * Mindis vertx application carcass.
 * Defines initialization of ApplicationContext and per-verticle BeanContexts.
 */
@Slf4j
abstract class MindisVertxApplication {
    static final PROP_IS_APP_BEAN_CTX = 'vertx.app.bean.ctx'

    volatile ApplicationContext applicationContext
    final Map<String, Tuple2<String, BeanContext>> beanContexts = new ConcurrentHashMap<>()

    final Completable start() {
        Completable
                .fromAction {
                    def isRunning = applicationContext as Boolean
                    if (isRunning) {
                        return Completable.error(new MindisException('application is already running'))
                    }
                }

                .andThen(startAppContext())
                .doOnSubscribe { log.info 'starting application' }
                .doOnSuccess { applicationContext = it }

                .flatMapCompletable { appCtx ->
                    def vertx = appCtx.getBean(Vertx)
                    Observable
                            .fromIterable(verticleNames)

                    // create bean context for each verticle before deploying the verticle
                            .map {
                                log.debug 'creating bean context for verticle: verticle={}', it
                                def beanCtx = BeanContext.run()
                                log.debug 'bean context for verticle created: verticle={}, beanCtx={}',
                                        it, beanCtx
                                [verticleName: it, beanContext: beanCtx]
                            }
                            .doOnNext {
                                log.debug 'inject app beans into verticle bean context: verticle={}, beanCtx={}',
                                        it.verticleName, it.beanContext
                                injectAppBeansToVerticleBeanContext(it.beanContext, appCtx)
                                log.debug 'app beans injected into verticle bean context: verticle={}, beanCtx={}',
                                        it.verticleName, it.beanContext
                            }

                    // and now deploy the verticle
                            .flatMapCompletable {
                                def verticleName = it.verticleName as String
                                def beanCtx = it.beanContext as BeanContext
                                log.debug 'deploying verticle: verticleName={}', verticleName
                                vertx.rxDeployVerticle(beanCtx.getBean(verticleName as Class) as MindisVerticle)
                                        .doOnSuccess {
                                            log.debug 'deployed verticle: verticleName={}, depId={}, beanCtx={}',
                                                    verticleName, it, beanCtx
                                            beanContexts[it] = new Tuple2<>(verticleName, beanCtx)
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
                    def appCtx = applicationContext
                    def isRunning = appCtx as Boolean
                    if (!isRunning) {
                        return Completable.error(new MindisException('application is already stopped'))
                    }
                    appCtx
                }

        // stop vertx
                .flatMap { appCtx ->
                    appCtx.getBean(Vertx)
                            .rxClose()
                            .doOnSubscribe { log.info 'stopping application' }
                            .doOnSubscribe { log.debug 'closing vertx' }
                            .doOnComplete { log.debug 'vertx closed' }
                            .toSingleDefault(appCtx)
                }

        // stop bean context for each verticle
                .flatMapObservable { appCtx ->
                    Observable
                            .fromIterable(beanContexts.collect {
                                [deploymentId: it.key, verticleName: it.value.v1, beanContext: it.value.v2]
                            })
                            .doOnSubscribe { log.debug 'stopping all bean contexts' }
                            .doOnNext {
                                log.debug 'closing bean context: verticleName={}, depId={}, beanCtx={}',
                                        it.verticleName, it.deploymentId, it.beanContext
                                it.beanContext.stop()
                                beanContexts.remove(it.deploymentId)
                                log.debug 'bean context closed: verticleName={}, depId={}, beanCtx={}',
                                        it.verticleName, it.deploymentId, it.beanContext
                            }
                            .doOnComplete { log.debug 'all bean contexts stopped' }

                    // stop main application bean context
                            .concatWith(Completable.fromAction {
                                log.debug 'stopping application context: appCtx={}', appCtx
                                appCtx.stop()
                                applicationContext = null
                                log.debug 'application context sopped: appCtx={}', appCtx
                            })
                }
                .ignoreElements()
                .doOnComplete { log.info 'application stopped' }
                .doOnError { log.error 'failed to stop application', it }
    }

    abstract List<String> getVerticleNames()

    private Single<ApplicationContext> startAppContext() {
        Single.<ApplicationContext> fromCallable {
            log.debug 'starting application bean context'
            def appCtx = ApplicationContext.build()
                    .properties((PROP_IS_APP_BEAN_CTX): true)
                    .start()
            log.debug 'application bean context started'
            appCtx
        }
    }

    protected void injectAppBeansToVerticleBeanContext(BeanContext beanCtx, ApplicationContext appCtx) {
        [[ApplicationContext, appCtx],
         [Vertx, appCtx.getBean(Vertx)]
        ].each { Class beanType, def bean ->
            log.debug 'inject app bean: type={}, bean={}, beanCtx={}',
                    beanType, bean, beanCtx
            beanCtx.registerSingleton(beanType, bean)
        }
    }

    protected Logger getLog() {
        log
    }
}
