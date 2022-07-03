package com.dburyak.vertx.core.di;

import jakarta.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Scope
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface WorkerScope {
    // TODO: implement
}
