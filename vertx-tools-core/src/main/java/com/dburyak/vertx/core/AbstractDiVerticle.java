package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.VerticleBeanBaseClass;
import io.micronaut.context.ApplicationContext;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.core.AbstractVerticle;
import jakarta.inject.Inject;
import lombok.Setter;

/**
 * Verticle base class with DI support.
 * <p>
 * This is a base building block for actor-based system that adds important features on top of Vertx
 * {@link io.vertx.core.AbstractVerticle}/${@link AbstractVerticle}. Implementations should be used through
 * {@link VertxApp} instead of default Vertx mechanisms otherwise DI won't work.
 * <p>
 * This base class has already all the necessary scope annotations in place. Only
 * {@link io.micronaut.context.annotation.Bean} annotations may be needed to explicitly mark it as a bean. This may fix
 * issues with lombok {@link Setter#onMethod} or {@link Setter#onParam} used in DiVerticles.
 * Subclasses should not put any scope annotations since instantiation and initialization of DI Verticles
 * happens not through regular DI mechanisms but through manual bean creation and injection.
 * Instead, use standard vertx verticle {@link io.vertx.core.DeploymentOptions} specified as part of
 * {@link VerticleDeploymentDescriptor} provided for {@link VertxApp} for controlling number of instances.
 * <p>
 * DiVerticle deployment happens in five phases:
 * <ul>
 *     <li>verticle instantiation with default constructor on the calling thread
 *     <li>deployment initial phase on the calling thread
 *     <li>dependency injection on the vertx event loop thread
 *     <li>verticle initialization routine on the vertx event loop thread
 *     <li>verticle startup
 * </ul>
 * Thus, all implementations must provide default constructor for first phase.
 * Dependency injection through constructor arguments is not possible due to vertx internal design - it creates
 * verticle instance on the calling thread, not on the event loop context thread.
 * Performing beans injection on vertx event loop thread allows to avoid visibility issues when using stateful
 * prototype-scoped, or event-loop-scoped, or verticle-scoped beans. This approach makes it possible to take advantage
 * of all the single-threaded threading model benefits without putting any effort in safe publishing of
 * stateful beans code. The tradeoff - unable to use constructor-based injection on verticles. Though constructor-based
 * injection can be safely used on any beans other than verticles.
 */
@VerticleBeanBaseClass
public abstract class AbstractDiVerticle extends AbstractVerticle {

    @Setter
    protected volatile ApplicationContext appCtx;

    @Override
    public final void init(Vertx vertx, Context context) {
        appCtx.registerSingleton(this);
        doOnInit(vertx, context);
    }

    /**
     * Subclasses can extend verticle initialization behavior with this method.
     *
     * @param vertx vertx
     * @param context vertx execution context
     */
    protected void doOnInit(Vertx vertx, Context context) {
        // subclasses can extend verticle initialization behavior with this method
    }

    @Inject
    protected void setVertx(io.vertx.rxjava3.core.Vertx vertx) {
        this.vertx = vertx;
    }
}
