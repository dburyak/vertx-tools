package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;

import java.util.concurrent.AbstractExecutorService;

public abstract class AbstractVertxCtxExecutorService extends AbstractExecutorService {
    protected final Context vertxCtx;

    protected AbstractVertxCtxExecutorService(Context vertxCtx) {
        this.vertxCtx = vertxCtx;
    }

    protected AbstractVertxCtxExecutorService(Vertx vertx) {
        this(vertx.getOrCreateContext());
    }

    @Override
    public final void execute(Runnable action) {
        vertxCtx.runOnContext(v -> action.run());
    }
}
