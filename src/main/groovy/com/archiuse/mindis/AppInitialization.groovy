package com.archiuse.mindis

import groovy.util.logging.Slf4j
import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins

import javax.annotation.PostConstruct

@Slf4j
class AppInitialization {
    Scheduler vertxRxScheduler
    Scheduler vertxRxBlockingScheduler

    @PostConstruct
    void init() {
        registerRxJavaVertxSchedulers()
    }

    private void registerRxJavaVertxSchedulers() {
        log.debug 'register application wide vertx rxjava schedulers'
        RxJavaPlugins.computationSchedulerHandler = { vertxRxScheduler }
        RxJavaPlugins.ioSchedulerHandler = { vertxRxBlockingScheduler }
        RxJavaPlugins.newThreadSchedulerHandler = { vertxRxScheduler }
    }
}
