package com.archiuse.mindis

import com.archiuse.mindis.di.AppBean
import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
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
    final Map<String, Tuple2<String, ApplicationContext>> verticlesBeanContexts = new ConcurrentHashMap<>()

    final Completable start() {
        Completable
                .fromAction {
                    def isRunning = applicationBeanContext as Boolean
                    if (isRunning) {
                        return Completable.error(new MindisException('application is already running'))
                    }
                }

                .andThen(startAppContext())
                .doOnSubscribe { log.info 'starting application' }
                .doOnSuccess { applicationBeanContext = it }

                .flatMapCompletable { mainCtx ->
                    def vertx = mainCtx.getBean(Vertx)
                    Observable
                            .fromIterable(verticleNames)

                    // create per-verticle bean context for each verticle before deploying the verticle
                            .map {
                                log.debug 'creating bean context for verticle: verticle={}', it
                                def verticleCtx = ApplicationContext.build()
                                        .properties((PROP_IS_APP_BEAN_CTX): false)
                                        .start()
                                log.debug 'bean context for verticle created: verticle={}, beanCtx={}',
                                        it, verticleCtx
                                [verticleName: it, beanContext: verticleCtx]
                            }
                            .doOnNext {
                                log.debug 'inject main app beans into verticle bean context: verticle={}, beanCtx={}',
                                        it.verticleName, it.beanContext
                                injectMainBeansToVerticleBeanContext(it.beanContext, mainCtx)
                                log.debug 'main app beans injected into verticle bean context: verticle={}, beanCtx={}',
                                        it.verticleName, it.beanContext
                            }

                    // and now deploy the verticle
                            .flatMapCompletable {
                                def verticleName = it.verticleName as String
                                def beanCtx = it.beanContext as ApplicationContext
                                log.debug 'deploying verticle: verticleName={}', verticleName
                                vertx.rxDeployVerticle(beanCtx.getBean(verticleName as Class) as MindisVerticle)
                                        .doOnSuccess {
                                            log.debug 'deployed verticle: verticleName={}, depId={}, beanCtx={}',
                                                    verticleName, it, beanCtx
                                            verticlesBeanContexts[it] = new Tuple2<>(verticleName, beanCtx)
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
                        return Completable.error(new MindisException('application is already stopped'))
                    }
                    mainCtx
                }

        // stop vertx
                .flatMap { mainCtx ->
                    mainCtx.getBean(Vertx)
                            .rxClose()
                            .doOnSubscribe { log.info 'stopping application' }
                            .doOnSubscribe { log.debug 'closing vertx' }
                            .doOnComplete { log.debug 'vertx closed' }
                            .toSingleDefault(mainCtx)
                }

        // stop bean context for each verticle
                .flatMapObservable { mainCtx ->
                    Observable
                            .fromIterable(verticlesBeanContexts.collect {
                                [deploymentId: it.key, verticleName: it.value.v1, beanContext: it.value.v2]
                            })
                            .doOnSubscribe { log.debug 'stopping all verticle bean contexts' }
                            .doOnNext {
                                log.debug 'closing bean context: verticleName={}, depId={}, beanCtx={}',
                                        it.verticleName, it.deploymentId, it.beanContext
                                it.beanContext.stop()
                                verticlesBeanContexts.remove(it.deploymentId)
                                log.debug 'bean context closed: verticleName={}, depId={}, beanCtx={}',
                                        it.verticleName, it.deploymentId, it.beanContext
                            }
                            .doOnComplete { log.debug 'all verticle bean contexts stopped' }

                    // stop main application bean context
                            .concatWith(Completable.fromAction {
                                log.debug 'stopping application main bean context: appCtx={}', mainCtx
                                mainCtx.stop()
                                applicationBeanContext = null
                                log.debug 'application main bean context sopped: appCtx={}', mainCtx
                            })
                }
                .ignoreElements()
                .doOnComplete { log.info 'application stopped' }
                .doOnError { log.error 'failed to stop application', it }
    }

    abstract List<String> getVerticleNames()

    private Single<ApplicationContext> startAppContext() {
        Single.<ApplicationContext> fromCallable {
            log.debug 'starting main bean context'
            def appCtx = ApplicationContext.build()
                    .properties((PROP_IS_APP_BEAN_CTX): true)
                    .start()
            log.debug 'main bean context started'
            appCtx
        }
    }

    protected void injectMainBeansToVerticleBeanContext(BeanContext beanCtx, ApplicationContext appCtx) {
        [[ApplicationContext, appCtx, Qualifiers.byStereotype(AppBean)],
         [Vertx, appCtx.getBean(Vertx), null]
        ].each { Class beanType, def bean, Qualifier qualifier ->
            log.debug 'inject app bean: type={}, bean={}, beanCtx={}',
                    beanType, bean, beanCtx
            if (qualifier) {
                beanCtx.registerSingleton(beanType, bean, qualifier)
            } else {
                beanCtx.registerSingleton(beanType, bean)
            }
        }
    }

    protected Logger getLog() {
        log
    }
}
