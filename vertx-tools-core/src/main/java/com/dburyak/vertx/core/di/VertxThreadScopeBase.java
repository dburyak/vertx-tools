package com.dburyak.vertx.core.di;

import com.dburyak.vertx.core.AsyncCloseable;
import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.inject.BeanIdentifier;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

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
public abstract class VertxThreadScopeBase<T extends Annotation> extends AbstractConcurrentCustomScope<T> {

    /**
     * Map of all beans created in the scope. Key is thread name. Value is map of beans created for given thread.
     */
    protected final Map<String, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param annotationType scope annotation type specific for the implementation subclass
     */
    protected VertxThreadScopeBase(Class<T> annotationType) {
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
     * Synchronously destroy all beans in the scope on close.
     */
    @Override
    public final void close() {
        beans.values().forEach(this::destroyScope);
    }

    /**
     * Asynchronously dispose of all {@link com.dburyak.vertx.core.AsyncCloseable} beans in the scope for ALL vertx
     * contexts/threads.
     * This method is supposed to be called right before the synchronous {@link #stop()} method for the scope, during
     * the application shutdown routine.
     */
    public final Completable stopAsync() {
        return Observable.fromIterable(beans.values())
                .flatMapIterable(Map::values)
                .map(CreatedBean::bean)
                .filter(AsyncCloseable.class::isInstance)
                .cast(AsyncCloseable.class)
                .flatMapCompletable(AsyncCloseable::closeAsync);
        // Note that we do not remove the beans from the container map yet, as that will be done in the synchronous
        // close() method that is supposed to be called right after the resulting Completable is completed.
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

    private Map<BeanIdentifier, CreatedBean<?>> getVertxCtxBeans(boolean assertOnVertxCtx) {
        if (assertOnVertxCtx && !vertxThreadMatches()) {
            // turned out that Vertx.currentContext().isEventLoopContext() and Vertx.currentContext().isWorkerContext()
            // is not a reliable way to check if current thread is vertx one or not. Sometimes Vertx.currentContext()
            // returns null on EL threads. Not sure what this behavior depends on. So the simplest way is to check by
            // thread name.
            throw new IllegalArgumentException(notOnCtxErrorMessage());
        }
        return beans.computeIfAbsent(Thread.currentThread().getName(), tn -> new HashMap<>());
    }
}
