package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.AppBootstrap;
import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@AppBootstrap
@Singleton
@Slf4j
public class RxSchedulersInitializer {
    private final Scheduler vertxRxScheduler;
    private final Scheduler vertxRxBlockingScheduler;

    public RxSchedulersInitializer(@ForEventLoop Scheduler vertxRxScheduler,
            @ForWorker Scheduler vertxRxBlockingScheduler) {
        this.vertxRxScheduler = vertxRxScheduler;
        this.vertxRxBlockingScheduler = vertxRxBlockingScheduler;
    }

    @PostConstruct
    void init() {
        log.info("configure rx schedulers for vertx");
        RxJavaPlugins.setComputationSchedulerHandler(ignr -> vertxRxScheduler);
        RxJavaPlugins.setIoSchedulerHandler(ignr -> vertxRxBlockingScheduler);
        RxJavaPlugins.setNewThreadSchedulerHandler(ignr -> vertxRxScheduler);
    }
}
