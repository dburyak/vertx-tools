package com.dburyak.vertx.core.di;

import jakarta.inject.Singleton;

/**
 * Vertx event loop bean scope implementation.
 *
 * @see EventLoopScope
 */
@Singleton
public class EventLoopScopeImpl extends VertxThreadScopeBase<EventLoopScope> {

    /**
     * Default constructor.
     */
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
