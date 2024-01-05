package com.dburyak.vertx.core.executor;

import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class AbstractVertxCtxScheduledExecutorService extends AbstractExecutorService
        implements ScheduledExecutorService {
    protected final Context vertxCtx;

    protected AbstractVertxCtxScheduledExecutorService(Vertx vertx) {
        this(vertx.getOrCreateContext());
    }

    @Override
    public void execute(Runnable action) {
        vertxCtx.runOnContext(v -> action.run());
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable action, long delay, TimeUnit unit) {
        // TODO: implement
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        // TODO: implement
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        // TODO: implement
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        // TODO: implement
        throw new UnsupportedOperationException("not implemented");
    }
}
