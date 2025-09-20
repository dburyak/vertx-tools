package com.dburyak.vertx.core.di;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker qualifier annotation for beans that need to be initialized very early on the application startup, before
 * deploying any verticles or switching to vertx event loop. Note that such beans initialization may or may not happen
 * on the vertx event loop, depending on timing which is not guaranteed. If you need to do some initialization work on
 * the vertx event loop, consider using {@link AppStartup} or {@link VerticleStartup} beans.
 * <p>
 * Beans are requested only once per application, so they should be singletons. They are expected to do their
 * initialization work in {@link jakarta.annotation.PostConstruct} methods.
 * <p>
 * Typical usage: register RxJava plugins and schedulers to do proper vertx threading with RxJava, configure jackson
 * object mapper, etc.
 * <p>
 * <b>WARNING:</b> use this annotation sparingly, as it results in eager initialization of beans that are present on
 * the classpath but are not actually needed for the application. At best, this will result in a slightly longer startup
 * time, at worst - in initialization of resources that are not actually needed and may even lead to errors and
 * application startup failure.
 */
@Qualifier
@Retention(RUNTIME)
@Documented
public @interface AppBootstrap {
}
