package com.dburyak.vertx.test;

import com.dburyak.vertx.core.di.EventLoopScope;
import io.micronaut.context.annotation.Bean;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Bean
@EventLoopScope
@Slf4j
public class SampleEventLoopBean {
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
        log.info("dispose event loop bean: this={}", this);
    }
}
