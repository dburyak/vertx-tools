package com.dburyak.vertx.test;

import com.dburyak.vertx.core.di.AppBootstrap;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@AppBootstrap
@Singleton
@Slf4j
public class SampleBootstrapBean {

    @PostConstruct
    public void init() {
        log.info("SampleBootstrapBean initialized");
    }
}
