package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.EventLoop;
import com.dburyak.vertx.core.di.Worker;
import io.micronaut.context.annotation.Context;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

@Context
@Slf4j
public class RxSchedulersInitializer {
    private final Scheduler vertxRxScheduler;
    private final Scheduler vertxRxBlockingScheduler;

    public RxSchedulersInitializer(@EventLoop Scheduler vertxRxScheduler,
            @Worker Scheduler vertxRxBlockingScheduler) {
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
