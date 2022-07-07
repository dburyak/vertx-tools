package com.dburyak.vertx.core.di;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class EventLoopScopeImpl extends VertxCtxScopeBase<EventLoopScope> {

    public EventLoopScopeImpl() {
        super(EventLoopScope.class);
    }

    @Override
    protected boolean vertxThreadMatches() {
        return Thread.currentThread().getName().startsWith("vert.x-eventloop-thread-");
    }

    @Override
    protected String notOnCtxErrorMessage() {
        return "not on vertx event loop thread: currentThread=" + Thread.currentThread().getName();
    }
}
