package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

/**
 * ScheduledExecutorService that executes tasks on one specific vertx context only. This implementation doesn't track
 * tasks state and doesn't support shutdown. This implementation behaves like executor service that shuts down
 * immediately. This is fail-safe version of {@link VertxCtxMinimalStrictScheduledExecutorService}.
 * <p>
 * Useful for cases when you need to execute tasks on vertx context, and you don't need to track tasks state or shutdown
 * executor service. Specific example: 3rd party library requires ScheduledExecutorService, but you know for sure that
 * it doesn't manage executor lifecycle and only executes tasks on it.
 */
public class VertxCtxMinimalScheduledExecutorService extends AbstractVertxCtxScheduledExecutorService {

    public VertxCtxMinimalScheduledExecutorService(Vertx vertx) {
        super(vertx);
    }

    public VertxCtxMinimalScheduledExecutorService(Context vertxCtx) {
        super(vertxCtx);
    }

    @Override
    public void shutdown() {
        // do nothing
    }

    @Override
    public List<Runnable> shutdownNow() {
        return emptyList();
    }

    @Override
    public boolean isShutdown() {
        return true;
    }

    @Override
    public boolean isTerminated() {
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }
}
