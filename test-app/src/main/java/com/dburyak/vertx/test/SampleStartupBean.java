package com.dburyak.vertx.test;

import com.dburyak.vertx.core.di.AppStartup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@AppStartup
@Singleton
@Slf4j
public class SampleStartupBean {

    @PostConstruct
    public void init() {
        log.info("SampleStartupBean initialized");
    }
}
