package com.dburyak.vertx.core;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Factory
@Secondary
@Slf4j
public class VertxFactory {

    @Singleton
    @Secondary
    public Vertx vertx(VertxOptions vertxOptions, Optional<ClusterManager> clusterManager) {
        var opts = vertxOptions;
        Single<Vertx> vertxFuture;
        if (clusterManager.isPresent()) {
            opts = new VertxOptions(vertxOptions)
                    .setClusterManager(clusterManager.get());
            vertxFuture = Vertx.rxClusteredVertx(opts);
        } else {
            vertxFuture = Single.just(Vertx.vertx(opts));
        }
        var vertx = vertxFuture.blockingGet();
        log.info("vertx created: vertx={}, opts={}", vertx, opts);
        return vertx;
    }

    @Singleton
    @Secondary
    public VertxOptions vertxOptions(List<VertxOptionsConfigurer> configurers) {
        var opts = new VertxOptions();
        for (var configurer : configurers) {
            opts = configurer.configure(opts);
        }
        return opts;
    }
}
