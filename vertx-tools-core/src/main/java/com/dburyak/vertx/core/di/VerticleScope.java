package com.dburyak.vertx.core.di;

import jakarta.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Verticle bean scope. Is destroyed when the verticle is undeployed.
 *
 * <p>Similar to the concept of "ThreadLocal" but works per-verticle instead of per-thread.
 * Should be used when different verticles should have different instances of the annotated bean.
 */
@Scope
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface VerticleScope {
}
