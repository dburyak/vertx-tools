package com.dburyak.vertx.test;

import com.dburyak.vertx.core.di.EventLoopScope;
import io.micronaut.context.annotation.Bean;
import lombok.extern.slf4j.Slf4j;

@Bean
@EventLoopScope
@Slf4j
public class SampleEventLoopBean {
    private int totalCalls = 0;

    public SampleEventLoopBean() {
        log.info("constructor new sample bean: this={}", this);
    }

    public void hello() {
        totalCalls++;
        log.info("hello from bean: calls={}, this={}", totalCalls, this);
    }
}
