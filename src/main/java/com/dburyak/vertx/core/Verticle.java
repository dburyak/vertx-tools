package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.VerticleBean;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Primary;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.ext.healthchecks.CheckResult;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Context;
import io.vertx.reactivex.core.file.FileSystem;
import io.vertx.reactivex.ext.healthchecks.HealthChecks;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;

import static io.micronaut.inject.qualifiers.Qualifiers.byQualifiers;
import static io.micronaut.inject.qualifiers.Qualifiers.byStereotype;

/**
 * Verticle base class with DI support and monitoring.
 * This is a base building block for actor-based system that adds important features on top of Vertx {@link
 * AbstractVerticle}. Implementations should be used through {@link MicronautVertxApplication} instead of default Vertx
 * mechanisms otherwise DI won't work.
 *
 * <p>Implementations of this abstract class should also go with dedicated {@link MicronautVerticleProducer}
 * implementation.
 */
@Singleton
@Slf4j
public abstract class Verticle extends AbstractVerticle {

    @Getter
    private volatile Context verticleVertxCtx;

    @Getter
    protected volatile ApplicationContext verticleBeanCtx;

    @Getter
    private String version;

    @Getter
    private String revision;

    @Getter
    private String builtAt;

    @Setter(onMethod_ = {@Inject})
    private FileSystem fs;

    @Setter(onMethod_ = {@Inject})
    private HealthChecks healthChecks;

    @Setter(onMethod_ = {@Inject})
    private HealthChecks readyChecks;

    /**
     * Start this verticle.
     *
     * <p>Is not designed to be called directly, rather is called by the {@link MicronautVertxApplication}
     * on startup. Also may be called indirectly via
     * {@link MicronautVertxApplication#deployVerticle(MicronautVerticleProducer)}.
     *
     * @return start operation status
     */
    @Override
    public final Completable rxStart() {
        verticleVertxCtx = new Context(super.context);
        return Completable
                .fromAction(() -> {
                    log.info("starting verticle: {}", this);
                    log.debug("starting verticle bean context: verticle={}, verticleCtx={}", this, verticleBeanCtx);
                    verticleBeanCtx.registerSingleton(ApplicationContext.class, verticleBeanCtx,
                            byQualifiers(byStereotype(Primary.class), byStereotype(VerticleBean.class)));
                    verticleBeanCtx.registerSingleton(this);
                    verticleBeanCtx.refreshBean(verticleBeanCtx.findBeanRegistration(verticleBeanCtx).orElseThrow()
                            .getIdentifier());
                    verticleBeanCtx.refreshBean(verticleBeanCtx.findBeanRegistration(this).orElseThrow()
                            .getIdentifier());
                })
                .andThen(Completable.defer(this::doOnStart))
                .andThen(Completable.fromRunnable(() -> {
                    // health and readiness checks won't be monitored until verticle is deployed, so there's no any
                    // difference when exactly we register them
                    registerHealthProcedures(healthChecks);
                    registerReadyProcedures(readyChecks);
                }))
                .doOnComplete(() -> log.info("verticle started: {}", this))
                .doOnError(e -> log.error("failed to start verticle: {}", this, e));
    }

    /**
     * Stop this verticle.
     *
     * <p>Is not designed to be called directly, rather is called by the {@link MicronautVertxApplication} on shutdown.
     * Also may be called indirectly via {@link MicronautVertxApplication#undeployVerticle(String)}.
     *
     * @return stop operation status
     */
    @Override
    public final Completable rxStop() {
        return Completable
                .fromAction(() -> log.info("stopping verticle: {}", this))
                .andThen(Completable.defer(this::doOnStop))
                .doOnComplete(() -> log.info("verticle stopped: {}", this))
                .doOnError(e -> log.error("failed to stop verticle: {}", this, e));
    }

    /**
     * Get information about this verticle.
     *
     * <p>Designed for inheritance, implementations may override default behavior.
     *
     * @return map with human readable description of this verticle, which by default includes version, revision and
     * build time
     */
    public Single<Map<String, Object>> about() {
        return Single.just(Collections.emptyMap());
    }

    /**
     * Get health status of this verticle.
     *
     * <p>Unhealthy verticle may be restarted to solve issues. So as a rule of thumb health checks should include some
     * either high level goals of the verticle, or liveness indicator of some vital and fragile resource that is easy
     * to misuse/break in some edge cases.
     *
     * <p>Examples:
     * <ul>
     *     <li>Some activity managed by this verticle stopped responding (no heartbeats for example), probably due to
     *     some deadlock</li>
     *     <li>Owned pool doesn't provide resource for a long time, for example there's no db connection available in
     *     the pool for some time</li>
     *     <li>Last successful sent outgoing message was long time ago, which is the main high level goal of this
     *     example verticle. Note that such check should not be a simple "remote resource is available" but rather
     *     "able to do main job" check, since "remote resource is available" can not ever be fixed by verticle
     *     restart, however if failure is due to some error in the verticle itself, then restart should help</li>
     *     <li>Resources consumed by the verticle went too high for a long period, which may indicate a resource leak.
     *     It's often a good strategy to be prepared for such cases, to detect those, and design a recovery from
     *     unknown failure rather than blindly hope for the best.</li>
     * </ul>
     *
     * @return health check result
     */
    public final Single<CheckResult> health() {
        return isDeployed() ? healthChecks.rxCheckStatus()
                : Single.error(() -> new IllegalStateException("can not assess health of a not deployed verticle"));
    }

    /**
     * Get readiness status of this verticle.
     *
     * <p>Verticle that is not ready is not a subject for being restarted, but not yet a fully operational unit. I.e. it
     * won't be restarted but all its endpoints won't yet be advertised through service discovery, so it won't be
     * accepting any incoming calls/requests until it becomes ready. In "not ready" state it can make requests to other
     * verticles though, what can be a regular initialization routine.
     *
     * <p>Examples:
     * <ul>
     *     <li>Not yet connected to a database</li>
     *     <li>Not yet retrieved auth token for making outbound calls to external system</li>
     *     <li>Not yet fetched initial data to be served</li>
     * </ul>
     *
     * @return readiness check result
     */
    public final Single<CheckResult> ready() {
        return isDeployed() ? readyChecks.rxCheckStatus()
                : Single.error(() -> new IllegalStateException("can not assess readiness of a not deployed verticle"));
    }

    /**
     * Hook to add health check procedures for the verticle, subclasses should override this method to add custom health
     * checks. By default, doesn't register any health checks, so by default verticle is always healthy.
     *
     * @param healthChecks health checks to be customized
     */
    protected void registerHealthProcedures(HealthChecks healthChecks) {
    }

    /**
     * Hook to add readiness check procedures for the verticle, subclasses should override this method to add custom
     * readiness checks. By default, doesn't register any readiness checks, so by default verticle is always ready.
     *
     * @param readyChecks readiness checks to be customized
     */
    protected void registerReadyProcedures(HealthChecks readyChecks) {
    }

    protected Completable doOnStart() {
        return Completable.complete();
    }

    protected Completable doOnStop() {
        return Completable.complete();
    }

    protected final boolean isDeployed() {
        return context != null && context.deploymentID() != null;
    }
}
