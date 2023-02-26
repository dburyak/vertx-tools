package com.dburyak.vertx.core.di;

import jakarta.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Thread local bean scope.
 * <p>
 * Periodically checks if threads with beans are still alive and cleans up dead ones.
 */
@Scope
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ThreadLocalScope {
}
