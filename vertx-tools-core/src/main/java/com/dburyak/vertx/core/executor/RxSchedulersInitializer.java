package com.dburyak.vertx.core.executor;

import com.dburyak.vertx.core.di.AppBootstrap;
import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes RxJava schedulers for vertx. This makes rxjava scheduling operators to use proper vertx threads. Example
 * operators - interval, timer, delay, throttle, etc.
 */
@AppBootstrap
@Singleton
@Slf4j
public class RxSchedulersInitializer {
    private final Scheduler vertxRxScheduler;
    private final Scheduler vertxRxBlockingScheduler;

    /**
     * Constructor.
     *
     * @param vertxRxScheduler scheduler for event loop threads
     * @param vertxRxBlockingScheduler scheduler for worker threads
     */
    public RxSchedulersInitializer(@ForEventLoop Scheduler vertxRxScheduler,
            @ForWorker Scheduler vertxRxBlockingScheduler) {
        this.vertxRxScheduler = vertxRxScheduler;
        this.vertxRxBlockingScheduler = vertxRxBlockingScheduler;
    }

    @PostConstruct
    void init() {
        log.debug("configure rx schedulers for vertx");
        RxJavaPlugins.setComputationSchedulerHandler(ignr -> vertxRxScheduler);
        RxJavaPlugins.setIoSchedulerHandler(ignr -> vertxRxBlockingScheduler);
        RxJavaPlugins.setNewThreadSchedulerHandler(ignr -> vertxRxScheduler);
    }
}
