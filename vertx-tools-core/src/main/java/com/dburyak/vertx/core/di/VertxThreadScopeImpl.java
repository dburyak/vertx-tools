package com.dburyak.vertx.core.di;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Vertx thread bean scope implementation.
 */
@Singleton
@Slf4j
public class VertxThreadScopeImpl extends VertxCtxScopeBase<VertxThreadScope> {

    /**
     * Default constructor.
     */
    public VertxThreadScopeImpl() {
        super(VertxThreadScope.class);
    }

    @Override
    protected boolean vertxThreadMatches() {
        var currentThreadName = Thread.currentThread().getName();

        // FIXME: remove this debug output
        var matches = currentThreadName.startsWith("vert.x-eventloop-thread-")
                || currentThreadName.startsWith("vert.x-worker-thread-");

        log.debug("check thread name match: currentThreadName={}, matches={}", currentThreadName, matches);

        return matches;
    }

    @Override
    protected String notOnCtxErrorMessage() {
        return "not on vertx thread: currentThread=" + Thread.currentThread().getName();
    }
}
