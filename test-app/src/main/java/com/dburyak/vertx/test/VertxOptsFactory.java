package com.dburyak.vertx.test;

import com.dburyak.vertx.core.VertxOptionsConfigurer;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.concurrent.TimeUnit;

@Factory
public class VertxOptsFactory {

    @Singleton
    @Named("eventLoopSizeConfigurer")
    public VertxOptionsConfigurer eventLoopSizeConfigurer() {
        var numCpu = Runtime.getRuntime().availableProcessors();
        return opts -> opts.setEventLoopPoolSize(numCpu)
                .setBlockedThreadCheckInterval(10_000)
                .setBlockedThreadCheckIntervalUnit(TimeUnit.MILLISECONDS);
    }
}
