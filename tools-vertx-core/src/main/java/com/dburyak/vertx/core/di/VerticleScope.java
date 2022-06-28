package com.dburyak.vertx.core.di;

import jakarta.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Verticle bean scope.
 *
 * <p>Similar to the concept of "ThreadLocal" but works per-verticle instead of per-thread.
 * Should be used when different verticles should have different instances of the annotated bean.
 */
@Scope
@Retention(RUNTIME)
@Documented
public @interface VerticleScope {
}
