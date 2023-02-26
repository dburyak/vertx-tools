package com.dburyak.vertx.core.di;

import jakarta.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Event loop bean scope.
 *
 * <p>Almost identical to "ThreadLocal" scope but injection will fail if injected on other thread than event loop.
 * Very useful for not-thread-safe stateful beans - just use this scope and don't put any effort in making it thread
 * safe, as this bean will always be called only on the same exact event loop thread.
 */
@Scope
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface EventLoopScope {
}
