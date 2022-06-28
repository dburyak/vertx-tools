package com.dburyak.vertx.core;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Factory
@Secondary
@Slf4j
public class VertxFactory {

    @Singleton
    @Secondary
    public Vertx vertx(VertxOptions vertxOptions) {
        var vertx = Vertx.rxClusteredVertx(vertxOptions)
                .blockingGet();
        log.info("vertx created : {}", vertx);
        return vertx;
    }

    @Singleton
    @Secondary
    public VertxOptions vertxOptions(ClusterManager clusterManager) {
        return new VertxOptions().setClusterManager(clusterManager);
    }

    @Singleton
    @Secondary
    public ClusterManager clusterManager() {
        return new HazelcastClusterManager();
    }
}
