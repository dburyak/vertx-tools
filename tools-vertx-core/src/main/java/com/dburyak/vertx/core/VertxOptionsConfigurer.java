package com.dburyak.vertx.core;

import io.vertx.core.VertxOptions;

public interface VertxOptionsConfigurer {
    VertxOptions configure(VertxOptions opts);
}
