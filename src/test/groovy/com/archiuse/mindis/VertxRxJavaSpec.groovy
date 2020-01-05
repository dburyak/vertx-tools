package com.archiuse.mindis


import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import io.vertx.reactivex.core.RxHelper
import io.vertx.reactivex.core.Vertx
import spock.lang.Shared
import spock.lang.Specification

abstract class VertxRxJavaSpec extends Specification {

    @Shared
    Vertx vertx = Vertx.vertx()

    @Shared
    Scheduler vertxRxScheduler = RxHelper.scheduler(vertx)

    @Shared
    Scheduler vertxRxBlockingScheduler = RxHelper.blockingScheduler(vertx)

    @Shared
    def originalRxJavaComputationSchedulerHook

    @Shared
    def originalRxJavaIoSchedulerHook

    @Shared
    def originalRxJavaNewThreadSchedulerHook

    void setupSpec() {
        originalRxJavaComputationSchedulerHook = RxJavaPlugins.computationSchedulerHandler
        originalRxJavaIoSchedulerHook = RxJavaPlugins.ioSchedulerHandler
        originalRxJavaNewThreadSchedulerHook = RxJavaPlugins.newThreadSchedulerHandler
    }

    void cleanupSpec() {
        RxJavaPlugins.computationSchedulerHandler = originalRxJavaComputationSchedulerHook
        RxJavaPlugins.ioSchedulerHandler = originalRxJavaIoSchedulerHook
        RxJavaPlugins.newThreadSchedulerHandler = originalRxJavaNewThreadSchedulerHook
    }

    void setup() {
        RxJavaPlugins.computationSchedulerHandler = { vertxRxScheduler }
        RxJavaPlugins.ioSchedulerHandler = { vertxRxBlockingScheduler }
        RxJavaPlugins.newThreadSchedulerHandler = { vertxRxScheduler }
    }
}
