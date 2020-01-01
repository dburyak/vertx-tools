package com.archiuse.mindis.app

import com.archiuse.mindis.di.AppBean
import com.archiuse.mindis.di.EventLoop
import com.archiuse.mindis.di.Worker
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Context
import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins

import javax.annotation.PostConstruct
import javax.inject.Inject

@Context
@AppBean
@Slf4j
class AppInitialization {

    @Inject
    @EventLoop
    Scheduler vertxRxScheduler

    @Inject
    @Worker
    Scheduler vertxRxBlockingScheduler

    @PostConstruct
    void init() {
        log.info 'initializing application'
        registerRxJavaVertxSchedulers()
        log.info 'application initialized'
    }

    private void registerRxJavaVertxSchedulers() {
        log.debug 'register vertx rx schedulers'
        RxJavaPlugins.computationSchedulerHandler = { vertxRxScheduler }
        RxJavaPlugins.ioSchedulerHandler = { vertxRxBlockingScheduler }
        RxJavaPlugins.newThreadSchedulerHandler = { vertxRxScheduler }
    }
}
