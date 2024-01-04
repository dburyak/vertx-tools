package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorService that executes tasks on one specific vertx context only. This implementation doesn't track tasks state
 * and doesn't support shutdown. If shutdown related methods are called, {@link UnsupportedOperationException} will be
 * thrown. This is fail-fast version of {@link VertxCtxMinimalExecutorService}.
 * <p>
 * Useful for cases when you need to execute tasks on vertx context, and you don't need to track tasks state or shutdown
 * executor service. Specific example: 3rd party library requires ExecutorService, but you know for sure that it doesn't
 * manage executor lifecycle and only executes tasks on it.
 */
public class VertxCtxMinimalStrictExecutorService extends AbstractVertxCtxExecutorService {
    private static final String MSG_UNSUPPORTED_OPERATION = "operation is not supported";

    public VertxCtxMinimalStrictExecutorService(Vertx vertx) {
        this(vertx.getOrCreateContext());
    }

    public VertxCtxMinimalStrictExecutorService(Context vertxCtx) {
        super(vertxCtx);
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED_OPERATION);
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean isShutdown() {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean isTerminated() {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED_OPERATION);
    }
}
