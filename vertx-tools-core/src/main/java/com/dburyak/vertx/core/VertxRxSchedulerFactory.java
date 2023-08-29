package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.ForWorker;
import io.micronaut.context.annotation.Factory;
import io.reactivex.rxjava3.core.Scheduler;
import io.vertx.rxjava3.core.RxHelper;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;

/**
 * Rxjava schedulers adapted for vertx concurrency model.
 */
@Factory
public class VertxRxSchedulerFactory {

    /**
     * Scheduler for event loop threads.
     *
     * @param vertx vertx instance
     *
     * @return scheduler for event loop threads
     */
    @Singleton
    @ForEventLoop
    public Scheduler vertxRxScheduler(Vertx vertx) {
        return RxHelper.scheduler(vertx);
    }

    /**
     * Scheduler for worker threads.
     *
     * @param vertx vertx instance
     *
     * @return scheduler for worker threads
     */
    @Singleton
    @ForWorker
    public Scheduler vertxRxBlockingScheduler(Vertx vertx) {
        return RxHelper.blockingScheduler(vertx);
    }
}
