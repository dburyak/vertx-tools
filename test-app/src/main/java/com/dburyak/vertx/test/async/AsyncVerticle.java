package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.AbstractDiVerticle;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Inject;

@Bean
public class AsyncVerticle extends AbstractDiVerticle {

    @Inject
    AsyncSingletonBean asyncSingletonBean;

    @Inject
    AsyncVerticleBean asyncVerticleBean;

    @Inject
    AsyncEventLoopBean asyncEventLoopBean;
}
