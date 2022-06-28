package com.dburyak.vertx.core.http;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Secondary;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;

@Factory
@Secondary
public class HttpFactory {

    @Prototype
    @Bean(preDestroy = "close")
    @Secondary
    public HttpServer httpServer(Vertx vertx) {
        return vertx.createHttpServer();
    }

    @Prototype
    @Secondary
    public Router router(Vertx vertx) {
        return Router.router(vertx);
    }
}
