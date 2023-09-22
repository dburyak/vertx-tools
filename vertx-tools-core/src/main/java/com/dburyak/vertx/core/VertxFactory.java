package com.dburyak.vertx.core;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Factory for vertx related beans.
 */
@Factory
@Slf4j
public class VertxFactory {

    /**
     * {@link Vertx} bean.
     *
     * @param vertxOptions vertx options
     * @param clusterManager cluster manager if any
     *
     * @return singleton instance of {@link Vertx}
     */
    @Singleton
    @Requires(missingBeans = Vertx.class)
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
        log.debug("vertx created: vertx={}, opts={}", vertx, opts);
        return vertx;
    }

    /**
     * Vertx options bean.
     *
     * @param configurers vertx options configurers
     *
     * @return vertx options
     */
    @Singleton
    @Requires(missingBeans = VertxOptions.class)
    public VertxOptions vertxOptions(List<VertxOptionsConfigurer> configurers) {
        var opts = new VertxOptions();
        for (var configurer : configurers) {
            opts = configurer.configure(opts);
        }
        return opts;
    }
}
