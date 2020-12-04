package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.AppBean;
import io.micronaut.context.annotation.Factory;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@AppBean
@Factory
@Slf4j
public class VertxFactory {

    @Singleton
    @AppBean
    public Vertx vertx(VertxOptions vertxOptions) {
        var vertx = Vertx.rxClusteredVertx(vertxOptions)
                .blockingGet();
        log.info("vertx created : {}", vertx);
        return vertx;
    }

    @Singleton
    @AppBean
    public VertxOptions vertxOptions(ClusterManager clusterManager) {
        return new VertxOptions().setClusterManager(clusterManager);
    }

    @Singleton
    @AppBean
    public ClusterManager clusterManager() {
        return new HazelcastClusterManager();
    }
}
