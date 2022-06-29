package com.dburyak.vertx.core.cluster.hazelcast;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import jakarta.inject.Singleton;

@Factory
public class ClusterManagerFactory {

    @Singleton
    @Secondary
    public ClusterManager clusterManager() {
        return new HazelcastClusterManager();
    }
}
