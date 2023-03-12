package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
import io.micronaut.context.annotation.Factory;
import io.reactivex.rxjava3.core.Scheduler;
import io.vertx.rxjava3.core.RxHelper;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;

@Factory
public class VertxRxSchedulerFactory {

    @Singleton
    @ForEventLoop
    public Scheduler vertxRxScheduler(Vertx vertx) {
        return RxHelper.scheduler(vertx);
    }

    @Singleton
    @ForWorker
    public Scheduler vertxRxBlockingScheduler(Vertx vertx) {
        return RxHelper.blockingScheduler(vertx);
    }
}
