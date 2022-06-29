package com.dburyak.vertx.core.di;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker qualifier annotation for beans that need to be initialized very early on the application startup, before
 * deploying any verticles.
 * <p>
 * Such beans are expected to do their initialization work in {@link jakarta.annotation.PostConstruct} methods.
 */
@Qualifier
@Retention(RUNTIME)
@Documented
public @interface AppInit {
}
