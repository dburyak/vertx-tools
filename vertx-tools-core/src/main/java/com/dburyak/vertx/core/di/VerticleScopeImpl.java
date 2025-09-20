package com.dburyak.vertx.core.di;

import com.dburyak.vertx.core.AsyncCloseable;
import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.inject.BeanIdentifier;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;

/**
 * Verticle bean scope implementation.
 */
@Singleton
public class VerticleScopeImpl extends AbstractConcurrentCustomScope<VerticleScope> {
    private final Map<String, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     */
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

    public Completable destroyScopeForThisVerticle() {
        var thisVerticleBeans = getVerticleBeans(true);
        // There's theoretical possibility that between getVerticleBeans() and destroyScope(thisVerticleBeans) calls
        // another verticle-scoped bean is created on the same verticle and we end up NOT calling closeAsync() and
        // destroyScope() for it. However, this is practically impossible as this method is supposed to be called
        // ONLY from the verticle shutdown routine, and at that time we are absolutely sure that there won't be any
        // more beans created on this verticle. So we can safely ignore this theoretical possibility.
        return Observable.fromStream(thisVerticleBeans.values().stream().map(CreatedBean::bean))
                .filter(AsyncCloseable.class::isInstance)
                .cast(AsyncCloseable.class)
                .flatMapCompletable(AsyncCloseable::closeAsync)
                .andThen(Completable.fromRunnable(() -> destroyScope(thisVerticleBeans)));
    }

    private Map<BeanIdentifier, CreatedBean<?>> getVerticleBeans(boolean assertOnVerticleCtx) {
        var verticleId = getVerticleDeploymentId();
        if (assertOnVerticleCtx && verticleId == null) {
            throw new IllegalArgumentException("not a verticle context: currentThread=" + Thread.currentThread());
        }
        return verticleId != null ? beans.computeIfAbsent(verticleId, vid -> new HashMap<>())
                : emptyMap();
    }

    private String getVerticleDeploymentId() {
        var vertxCtx = Vertx.currentContext();
        return vertxCtx != null ? vertxCtx.deploymentID() : null;
    }
}
