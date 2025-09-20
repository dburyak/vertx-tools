package com.dburyak.vertx.core.di;

import com.dburyak.vertx.core.AsyncInitializable;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker qualifier annotation for beans that need to be initialized early on each verticle startup. This happens after
 * {@link AppBootstrap} and {@link AppStartup} beans have been initialized, and after the dependencies of the verticle
 * are injected, but before calling the verticle startup code. Think of it as an extra verticle startup step that can be
 * added transparently to any verticle in a declarative way without modifying the verticle startup routine code.
 * <p>
 * Beans are requested for each verticle. They may be singletons that are initialized before the first verticle is
 * deployed, or they may have {@link VerticleScope} that are supposed to be instantiated and initialized for each
 * verticle instance. Such beans are expected to do their initialization work in
 * {@link jakarta.annotation.PostConstruct} methods, which is always called only once per bean instance, and invocation
 * happens on the corresponding verticle context. However, if bean implements {@link AsyncInitializable} interface, then
 * the async action invocation is repeated for each verticle instance on the corresponding verticle context.
 * <p>
 * If for some reason you intend to have a {@link VerticleStartup} singleton bean that implements
 * {@link AsyncInitializable} interface and you do not want the async action to be repeated for each verticle instance,
 * then you should take care of this in the async action implementation. The easiest one-line way to achieve this is by
 * using {@link io.reactivex.rxjava3.core.Completable#cache()} method.
 * <p>
 * Typical usage: initialize or preload verticle-specific resources transparently to the verticle without hardcoding the
 * initialization logic in the verticle startup method itself, decorate verticle-scoped beans with additional
 * functionality, etc.
 */
@Qualifier
@Retention(RUNTIME)
@Documented
public @interface VerticleStartup {

    /**
     * Verticle type that this startup bean is attached to. If the value is {@link Object}, then the startup bean is
     * added to all the verticles.
     * <p>
     * <b>WARNING:</b> better avoid using {@link Object} as the value here, as this may lead to eager initialization
     * of beans that are present on the classpath but are not actually needed for the application. However, there may be
     * valid use cases for this, when the async init logic is indeed applicable to any verticle.
     *
     * @return verticle type that this startup bean is attached to
     */
    Class<?> value();
}
