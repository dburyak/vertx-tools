package com.dburyak.vertx.core.di;

import jakarta.inject.Singleton;

/**
 * Vertx worker bean scope implementation.
 */
@Singleton
public class WorkerScopeImpl extends VertxThreadScopeBase<WorkerScope> {

    /**
     * Default constructor.
     */
    protected WorkerScopeImpl() {
        super(WorkerScope.class);
    }

    @Override
    protected boolean vertxThreadMatches() {
        return Thread.currentThread().getName().startsWith("vert.x-worker-thread-");
    }

    @Override
    protected String notOnCtxErrorMessage() {
        return "not on vertx worker thread: currentThread=" + Thread.currentThread().getName();
    }
}
