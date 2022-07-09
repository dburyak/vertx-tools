package com.dburyak.vertx.core.di;

import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.inject.BeanIdentifier;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;

@Singleton
public class VerticleScopeImpl extends AbstractConcurrentCustomScope<VerticleScope> {
    private final Map<String, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();

    public VerticleScopeImpl() {
        super(VerticleScope.class);
    }

    @Override
    protected Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation) {
        return getVerticleBeans(forCreation);
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void close() {
        beans.values().forEach(this::destroyScope);
    }

    private Map<BeanIdentifier, CreatedBean<?>> getVerticleBeans(boolean assertOnVerticleCtx) {
        var verticleId = getVerticleDeploymentId();
        if (assertOnVerticleCtx && verticleId == null) {
            throw new IllegalArgumentException("not verticle context: currentThread=" + Thread.currentThread());
        }
        return verticleId != null ? beans.computeIfAbsent(verticleId, vid -> new HashMap<>())
                : emptyMap();
    }

    private String getVerticleDeploymentId() {
        var vertxCtx = Vertx.currentContext();
        return vertxCtx != null ? vertxCtx.deploymentID() : null;
    }
}
