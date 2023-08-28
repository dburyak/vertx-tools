package com.dburyak.vertx.core.di;

import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.inject.BeanIdentifier;
import io.vertx.rxjava3.core.Vertx;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class VertxCtxScopeBase<T extends Annotation> extends AbstractConcurrentCustomScope<T> {
    protected final Map<String, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();

    protected VertxCtxScopeBase(Class<T> annotationType) {
        super(annotationType);
    }

    @Override
    protected Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation) {
        return getVertxCtxBeans(forCreation);
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void close() {
        beans.values().forEach(this::destroyScope);
    }

    /**
     * Subclass specific error message for the case when scope is accessed from unexpected thread.
     *
     * @return subclass specific error message
     */
    protected abstract String notOnCtxErrorMessage();

    protected abstract boolean vertxThreadMatches();

    private boolean isOnCtx() {
        var currentVertxContext = Vertx.currentContext();
        if (currentVertxContext == null
                || (!currentVertxContext.isEventLoopContext() && !currentVertxContext.isWorkerContext())) {
            return false;
        }
        return vertxThreadMatches();
    }

    private Map<BeanIdentifier, CreatedBean<?>> getVertxCtxBeans(boolean assertOnVertxCtx) {
        if (assertOnVertxCtx && !isOnCtx()) {
            throw new IllegalArgumentException(notOnCtxErrorMessage());
        }
        return beans.computeIfAbsent(Thread.currentThread().getName(), tn -> new HashMap<>());
    }
}
