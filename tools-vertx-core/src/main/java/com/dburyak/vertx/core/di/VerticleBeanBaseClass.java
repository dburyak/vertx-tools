package com.dburyak.vertx.core.di;

import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.context.annotation.Prototype;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Inherited meta-annotation that defines prototype bean scope by default for subclasses.
 */
@Prototype
@DefaultScope(Prototype.class)
@Inherited
@Retention(RUNTIME)
public @interface VerticleBeanBaseClass {
}
