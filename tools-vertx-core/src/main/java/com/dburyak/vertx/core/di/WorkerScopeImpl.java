package com.dburyak.vertx.core.di;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class WorkerScopeImpl extends VertxCtxScopeBase<WorkerScope> {

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
