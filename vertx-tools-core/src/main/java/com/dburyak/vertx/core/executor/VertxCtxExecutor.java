package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Executor;

/**
 * Executor that executes tasks on one specific vertx context only. Useful when tasks triggered outside vertx threads
 * need to be executed on vertx context (EL or worker thread).
 */
@RequiredArgsConstructor
public class VertxCtxExecutor implements Executor {
    protected final Context vertxCtx;

    public VertxCtxExecutor(Vertx vertx) {
        this(vertx.getOrCreateContext());
    }

    @Override
    public void execute(Runnable action) {
        vertxCtx.runOnContext(v -> action.run());
    }
}
