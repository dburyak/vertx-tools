package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AsyncAction;
import com.dburyak.vertx.core.di.VerticleStartup;
import io.reactivex.rxjava3.core.Completable;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@VerticleStartup(HelloVerticle2.class)
@Singleton
@Slf4j
public class SampleVerticleStartupAsyncBean implements AsyncAction {

    @Override
    public Completable execute() {
        return Completable.fromRunnable(() -> log.info("SampleVerticleStartupAsyncBean initialized - " +
                "async execute block - called for each matching verticle instance"));
    }
}
