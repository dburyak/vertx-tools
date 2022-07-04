package com.dburyak.vertx.core.di;

import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.inject.BeanIdentifier;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Slf4j
public class EventLoopScopeImpl extends AbstractConcurrentCustomScope<EventLoopScope> {
    private final Map<String, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();

    public EventLoopScopeImpl() {
        super(EventLoopScope.class);
    }

    @Override
    protected Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation) {
        return getEventLoopBeans(forCreation);
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void close() {
        beans.values().forEach(this::destroyScope);
    }

    private Map<BeanIdentifier, CreatedBean<?>> getEventLoopBeans(boolean assertOnEventLoop) {
        if (assertOnEventLoop && !isEventLoopContext()) {
            throw new IllegalArgumentException("not event loop context: currentThread=" + Thread.currentThread());
        }
        var elThreadName = Thread.currentThread().getName();
        return beans.computeIfAbsent(elThreadName, tn -> new HashMap<>());
    }

    private boolean isEventLoopContext() {
        var currentVertxContext = Vertx.currentContext();
        return currentVertxContext != null && currentVertxContext.isEventLoopContext();
    }
}
