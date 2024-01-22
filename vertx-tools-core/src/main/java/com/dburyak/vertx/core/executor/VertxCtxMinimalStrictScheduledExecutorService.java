package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledExecutorService that executes tasks on one specific vertx context only. This implementation doesn't track
 * tasks state and doesn't support shutdown. If shutdown related methods are called,
 * {@link UnsupportedOperationException} will be thrown. This is fail-fast version of
 * {@link VertxCtxMinimalScheduledExecutorService}.
 * <p>
 * Useful for cases when you need to execute tasks on vertx context, and you don't need to track tasks state or shutdown
 * executor service. Specific example: 3rd party library requires ScheduledExecutorService, but you know for sure that
 * it doesn't manage executor lifecycle and only executes tasks on it.
 */
public class VertxCtxMinimalStrictScheduledExecutorService extends AbstractVertxCtxScheduledExecutorService {
    private static final String MSG_UNSUPPORTED_OPERATION = "operation is not supported";

    public VertxCtxMinimalStrictScheduledExecutorService(Vertx vertx) {
        super(vertx);
    }

    public VertxCtxMinimalStrictScheduledExecutorService(Context vertxCtx) {
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
