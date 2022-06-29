package com.dburyak.vertx.core;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Factory
@Secondary
@Slf4j
public class VertxFactory {

    @Singleton
    @Secondary
    public Vertx vertx(VertxOptions vertxOptions, Optional<ClusterManager> clusterManager) {
        var vertx = clusterManager
                .map(cm -> {
                    var clusteredVertxOpts = new VertxOptions(vertxOptions);
                    clusteredVertxOpts.setClusterManager(cm);
                    return Vertx.clusteredVertx(clusteredVertxOpts);
                })
                .orElseGet(() -> Single.just(Vertx.vertx(vertxOptions)))
                .blockingGet();
        log.info("vertx created : {}", vertx);
        return vertx;
    }

    @Singleton
    @Secondary
    public VertxOptions vertxOptions() {
        return new VertxOptions();
    }
}
