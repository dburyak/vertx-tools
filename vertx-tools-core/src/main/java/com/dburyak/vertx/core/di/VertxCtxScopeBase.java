package com.dburyak.vertx.core.di;

import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.inject.BeanIdentifier;
import io.vertx.rxjava3.core.Vertx;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for DI bean scopes that are bound to vertx threads. This scope works as thread local scope but injection
 * is allowed only from specific kind of vertx threads.
 *
 * @param <T> scope annotation type
 */
public abstract class VertxCtxScopeBase<T extends Annotation> extends AbstractConcurrentCustomScope<T> {

    /**
     * Map of all beans created in the scope. Key is thread name. Value is map of beans created for given thread.
     */
    protected final Map<String, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param annotationType scope annotation type specific for the implementation subclass
     */
    protected VertxCtxScopeBase(Class<T> annotationType) {
        super(annotationType);
    }

    /**
     * Get beans map for current vertx context/thread.
     *
     * @param forCreation whether it is for bean creation (get or create bean) or for bean destruction (destroy
     *         bean)
     *
     * @return beans map for current thread
     */
    @Override
    protected final Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation) {
        return getVertxCtxBeans(forCreation);
    }

    /**
     * Always return true as no any special startup/initialization routine is required for this type of scope.
     *
     * @return true
     */
    @Override
    public final boolean isRunning() {
        return true;
    }

    /**
     * Destroy all beans in the scope on close.
     */
    @Override
    public final void close() {
        beans.values().forEach(this::destroyScope);
    }

    /**
     * Subclass specific error message for the case when scope is accessed from unexpected thread.
     *
     * @return subclass specific error message
     */
    protected abstract String notOnCtxErrorMessage();

    /**
     * Subclass specific check if current vertx thread is suitable for the scope.
     *
     * @return whether current thread is suitable for the scope
     */
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
