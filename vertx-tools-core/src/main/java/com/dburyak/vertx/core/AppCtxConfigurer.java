package com.dburyak.vertx.core;

import io.micronaut.context.ApplicationContextBuilder;

/**
 * Allows configuring application context (bean context) on the earliest stage of application startup. This is
 * particularly useful for tweaking config files discovery, environment variables mapping, etc.
 */
public interface AppCtxConfigurer {

    /**
     * Configure application context builder.
     *
     * @param appCtxBuilder application context builder
     *
     * @return configured application context builder. It's supposed to be the same instance as passed in for call
     *         chaining, but it may be a new instance if it makes any sense in some scenarios.
     */
    ApplicationContextBuilder configure(ApplicationContextBuilder appCtxBuilder);
}
