package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.EventLoop;
import com.dburyak.vertx.core.di.Worker;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.reactivex.rxjava3.core.Scheduler;
import io.vertx.rxjava3.core.RxHelper;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;

@Factory
@Secondary
public class VertxRxSchedulerFactory {

    @Singleton
    @EventLoop
    @Secondary
    public Scheduler vertxRxScheduler(Vertx vertx) {
        return RxHelper.scheduler(vertx);
    }

    @Singleton
    @Worker
    @Secondary
    public Scheduler vertxRxBlockingScheduler(Vertx vertx) {
        return RxHelper.blockingScheduler(vertx);
    }
}
