package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AsyncCloseable;
import com.dburyak.vertx.core.di.EventLoopScope;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Bean
@EventLoopScope
@Slf4j
public class SampleEventLoopBean implements AsyncCloseable {
    private int totalCalls = 0;

    public SampleEventLoopBean() {
        log.info("constructor new event loop bean: this={}", this);
    }

    public void hello() {
        totalCalls++;
        log.info("hello from event loop bean: calls={}, this={}", totalCalls, this);
    }

    @PreDestroy
    public void destroy() {
        log.info("blocking dispose event loop bean: this={}", this);
    }

    @Override
    public Completable closeAsync() {
        return Completable.fromRunnable(() -> log.info("async close event loop bean: this={}", this))
                .delaySubscription(100, MILLISECONDS);
    }
}
