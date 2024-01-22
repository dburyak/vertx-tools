package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

/**
 * ExecutorService that executes tasks on one specific vertx context only. This implementation doesn't track tasks state
 * and doesn't support shutdown. This implementation behaves like executor service that shuts down immediately. This is
 * fail-safe version of {@link VertxCtxMinimalStrictExecutorService}.
 * <p>
 * Useful for cases when you need to execute tasks on vertx context, and you don't need to track tasks state or shutdown
 * executor service. Specific example: 3rd party library requires ExecutorService, but you know for sure that it doesn't
 * manage executor lifecycle and only executes tasks on it.
 */
public class VertxCtxMinimalExecutorService extends AbstractVertxCtxExecutorService {

    public VertxCtxMinimalExecutorService(Context vertxCtx) {
        super(vertxCtx);
    }

    public VertxCtxMinimalExecutorService(Vertx vertx) {
        super(vertx);
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
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }
}
