package com.dburyak.vertx.cluster.hazelcast;

import com.hazelcast.config.Config;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Factory for Hazelcast default beans.
 */
@Factory
public class HazelcastClusterManagerFactory {

    /**
     * Default Hazelcast {@link ClusterManager} bean.
     *
     * @param hazelcastConfig hazelcast config
     *
     * @return hazelcast cluster manager
     */
    @Singleton
    @Requires(missingBeans = ClusterManager.class)
    public ClusterManager hazelcastClusterManager(Optional<Config> hazelcastConfig) {
        if (hazelcastConfig.isPresent()) {
            return new HazelcastClusterManager(hazelcastConfig.get());
        }
        return new HazelcastClusterManager();
    }
}
