package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.AbstractExecutorService;

import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class AbstractVertxCtxExecutorService extends AbstractExecutorService {
    protected final Context vertxCtx;

    protected AbstractVertxCtxExecutorService(Vertx vertx) {
        this(vertx.getOrCreateContext());
    }

    @Override
    public final void execute(Runnable action) {
        vertxCtx.runOnContext(v -> action.run());
    }
}
