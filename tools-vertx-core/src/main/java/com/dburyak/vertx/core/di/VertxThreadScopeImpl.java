package com.dburyak.vertx.core.di;

import jakarta.inject.Singleton;

@Singleton
public class VertxThreadScopeImpl extends VertxCtxScopeBase<VertxThreadScope> {

    public VertxThreadScopeImpl() {
        super(VertxThreadScope.class);
    }

    @Override
    protected boolean vertxThreadMatches() {
        var currentThreadName = Thread.currentThread().getName();
        return currentThreadName.startsWith("vert.x-eventloop-thread-")
                || currentThreadName.startsWith("vert.x-worker-thread-");
    }

    @Override
    protected String notOnCtxErrorMessage() {
        return "not on vertx thread: currentThread=" + Thread.currentThread().getName();
    }
}
