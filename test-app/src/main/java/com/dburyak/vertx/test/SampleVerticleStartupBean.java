package com.dburyak.vertx.test;

import com.dburyak.vertx.core.di.VerticleStartup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@VerticleStartup
@Singleton
@Slf4j
public class SampleVerticleStartupBean {

    @PostConstruct
    public void init() {
        log.info("SampleVerticleStartupBean initialized - sync PostConstruct init block - " +
                "called only once per bean instance");
    }
}
