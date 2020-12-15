package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.AppBean;
import com.dburyak.vertx.core.di.EventLoop;
import com.dburyak.vertx.core.di.Worker;
import io.micronaut.context.annotation.Context;
import io.reactivex.Scheduler;
import io.reactivex.plugins.RxJavaPlugins;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Context
@AppBean
@Slf4j
@Setter(onMethod_ = {@Inject})
public class AppInitialization {

    @EventLoop
    private Scheduler vertxRxScheduler;

    @Worker
    private Scheduler vertxRxBlockingScheduler;

    @PostConstruct
    final void init() {
        log.info("initializing application");
        registerRxJavaVertxSchedulers();
        doInit();
        log.info("application initialized");
    }

    protected void doInit() {
        // may be overridden by subclasses to introduce custom initialization logic
    }

    private void registerRxJavaVertxSchedulers() {
        log.debug("register vertx rx schedulers");
        RxJavaPlugins.setComputationSchedulerHandler(ignr -> vertxRxScheduler);
        RxJavaPlugins.setIoSchedulerHandler(ignr -> vertxRxBlockingScheduler);
        RxJavaPlugins.setNewThreadSchedulerHandler(ignr -> vertxRxScheduler);
    }
}
