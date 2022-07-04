package com.dburyak.vertx.test;

import com.dburyak.vertx.core.di.VerticleScope;
import io.micronaut.context.annotation.Bean;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Bean
@VerticleScope
@Slf4j
public class SampleVerticleBean {
    private int totalCalls = 0;

    public SampleVerticleBean() {
        log.info("constructor new verticle bean: this={}", this);
    }

    public void hello() {
        totalCalls++;
        log.info("hello from verticle bean: calls={}, this={}", totalCalls, this);
    }

    @PreDestroy
    public void destroy() {
        log.info("dispose verticle bean: this={}", this);
    }
}
