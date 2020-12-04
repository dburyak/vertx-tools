package com.dburyak.vertx.core.di;

import io.micronaut.context.annotation.Requires;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Application wide bean scope.
 * Should be used when the same bean instance should be shared across multiple verticles.
 */
@Qualifier
@Requires(property = "vertx.app.bean.ctx.main", value = "true")
@Retention(RUNTIME)
@Documented
public @interface AppBean {
}
