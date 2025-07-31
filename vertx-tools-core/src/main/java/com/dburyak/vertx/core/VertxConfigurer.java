package com.dburyak.vertx.core;

import io.vertx.rxjava3.core.VertxBuilder;

/**
 * Configures Vertx builder ({@link io.vertx.rxjava3.core.Vertx#builder()}). This allows building plugins that need to
 * configure Vertx instance being created.
 */
public interface VertxConfigurer {

    /**
     * Configure Vertx builder.
     *
     * @param vertxBuilder Vertx builder to configure
     *
     * @return configured Vertx builder (can be the same instance as passed in or a brand-new instance)
     */
    VertxBuilder configure(VertxBuilder vertxBuilder);
}
