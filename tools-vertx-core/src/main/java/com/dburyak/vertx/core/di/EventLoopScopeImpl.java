package com.dburyak.vertx.core.di;

import io.micronaut.context.BeanRegistration;
import io.micronaut.context.scope.BeanCreationContext;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.context.scope.CustomScope;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Slf4j
public class EventLoopScopeImpl implements CustomScope<EventLoopScope> {
    private final Map<String, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();

    // FIXME: re-implement it based off io.micronaut.context.scope.AbstractConcurrentCustomScope

    @Override
    public Class<EventLoopScope> annotationType() {
        return EventLoopScope.class;
    }

    @SuppressWarnings({"unchecked", "resource"})
    @Override
    public <T> T getOrCreate(BeanCreationContext<T> beanCreationCtx) {
        log.debug("get or create bean for EventLoopScope: {}", beanCreationCtx.id());
        return (T) getEventLoopBeans().computeIfAbsent(beanCreationCtx.id(), id -> {
                    var newBean = beanCreationCtx.create();
                    log.debug("create new EventLoopScope bean: id={}, bean={}", id, newBean);
                    return newBean;
                })
                .bean();
    }

    @Override
    public <T> Optional<T> remove(BeanIdentifier identifier) {
        log.debug("remove bean from EventLoopScope: {}", identifier);
        var createdBean = getEventLoopBeans().remove(identifier);
        if (createdBean == null) {
            return Optional.empty();
        }
        createdBean.close();
        return Optional.of((T) createdBean.bean());
    }

    @Override
    public <T> Optional<BeanRegistration<T>> findBeanRegistration(T bean) {
        log.info("findBeanRegistration for bean: {}", bean);
        return CustomScope.super.findBeanRegistration(bean);
    }

    @Override
    public <T> Optional<BeanRegistration<T>> findBeanRegistration(BeanDefinition<T> beanDefinition) {
        log.info("findBeanRegistration for beanDef: {}", beanDefinition);
        return CustomScope.super.findBeanRegistration(beanDefinition);
    }

    private String currentVertxEventLoopName() {
        var currentVertxContext = Vertx.currentContext();
        if (currentVertxContext == null || !currentVertxContext.isEventLoopContext()) {
            throw new IllegalArgumentException("not event loop context: currentThread=" + Thread.currentThread());
        }
        return Thread.currentThread().getName();
    }

    private Map<BeanIdentifier, CreatedBean<?>> getEventLoopBeans() {
        return beans.computeIfAbsent(currentVertxEventLoopName(), vertxCtx -> new HashMap<>());
    }
}
