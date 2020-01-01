package com.archiuse.mindis.app

import com.archiuse.mindis.di.AppBean
import com.archiuse.mindis.di.EventLoop
import com.archiuse.mindis.di.Worker
import io.micronaut.context.annotation.Factory
import io.reactivex.Scheduler
import io.vertx.reactivex.core.RxHelper
import io.vertx.reactivex.core.Vertx

import javax.inject.Singleton

@AppBean
@Factory
class VertxRxSchedulerFactory {

    @Singleton
    @AppBean
    @EventLoop
    Scheduler vertxRxScheduler(Vertx vertx) {
        RxHelper.scheduler vertx
    }

    @Singleton
    @AppBean
    @Worker
    Scheduler vertxRxBlockingScheduler(Vertx vertx) {
        RxHelper.blockingScheduler vertx
    }
}
