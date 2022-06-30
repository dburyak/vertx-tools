package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.PrototypeBeanBaseClass;
import io.micronaut.context.ApplicationContext;
import io.vertx.rxjava3.core.AbstractVerticle;
import lombok.RequiredArgsConstructor;

/**
 * Verticle base class with DI support.
 * <p>
 * This is a base building block for actor-based system that adds important features on top of Vertx
 * {@link io.vertx.core.AbstractVerticle}/${@link AbstractVerticle}. Implementations should be used through
 * {@link VertxDiApp} instead of default Vertx mechanisms otherwise DI won't work.
 * <p>
 * It's already marked as a prototype bean since more than one instance of verticle may be created. Implementations
 * are not supposed to override it, unless there's very specific need to do that.
 */
@PrototypeBeanBaseClass
@RequiredArgsConstructor
public abstract class DiVerticle extends AbstractVerticle {
    protected final ApplicationContext appCtx;
}

