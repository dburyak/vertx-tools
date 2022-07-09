package com.dburyak.vertx.test;

import com.dburyak.vertx.core.di.ThreadLocalScope;
import io.micronaut.context.annotation.Bean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Bean
@ThreadLocalScope
@Slf4j
public class SampleThreadLocalBean {

    @PostConstruct
    public void init() {
        log.info("created thread local bean: this={}, thread={}", this, Thread.currentThread());
    }

    @PreDestroy
    public void destroy() {
        log.info("destroy thread local bean: this={}, thread={}", this, Thread.currentThread());
    }
}
