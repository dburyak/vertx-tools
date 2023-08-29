package com.dburyak.vertx.core;

import io.vertx.core.VertxOptions;

/**
 * Configures vertx options. This allows to build plugins that need to configure vertx options.
 */
public interface VertxOptionsConfigurer {

    /**
     * Configure vertx options.
     *
     * @param opts vertx options to configure
     *
     * @return configured vertx options (cna be the same instance as passed in or new instance)
     */
    VertxOptions configure(VertxOptions opts);
}
