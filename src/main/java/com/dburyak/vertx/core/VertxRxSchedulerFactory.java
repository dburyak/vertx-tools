package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.EventLoop;
import com.dburyak.vertx.core.di.Worker;
import com.dburyak.vertx.core.di.AppBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.reactivex.Scheduler;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;

import javax.inject.Singleton;

@AppBean
@Factory
@Secondary
public class VertxRxSchedulerFactory {

    @Singleton
    @AppBean
    @EventLoop
    @Secondary
    public Scheduler vertxRxScheduler(Vertx vertx) {
        return RxHelper.scheduler(vertx);
    }

    @Singleton
    @AppBean
    @Worker
    @Secondary
    public Scheduler vertxRxBlockingScheduler(Vertx vertx) {
        return RxHelper.blockingScheduler(vertx);
    }
}
