package com.dburyak.vertx.core;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.VertxBuilder;
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
     * @param vertxBuilder configured vertx builder
     * @param clusterManager cluster manager if any
     *
     * @return singleton instance of {@link Vertx}
     */
    @Singleton
    @Requires(missingBeans = Vertx.class)
    public Vertx vertx(VertxBuilder vertxBuilder, Optional<ClusterManager> clusterManager) {
        Single<Vertx> vertxFuture;
        if (clusterManager.isPresent()) {
            vertxBuilder = vertxBuilder.withClusterManager(clusterManager.get());
            vertxFuture = vertxBuilder.rxBuildClustered();
        } else {
            vertxFuture = Single.just(vertxBuilder.build());
        }
        var vertx = vertxFuture.blockingGet();
        log.debug("vertx created: vertx={}", vertx);
        return vertx;
    }

    /**
     * Vertx builder bean.
     *
     * @param vertxOptions vertx options
     * @param configurers vertx builder configurers
     *
     * @return vertx builder
     */
    @Singleton
    @Requires(missingBeans = VertxBuilder.class)
    public VertxBuilder vertxBuilder(VertxOptions vertxOptions, List<VertxConfigurer> configurers) {
        var builder = Vertx.builder().with(vertxOptions);
        for (var configurer : configurers) {
            builder = configurer.configure(builder);
        }
        return builder;
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
