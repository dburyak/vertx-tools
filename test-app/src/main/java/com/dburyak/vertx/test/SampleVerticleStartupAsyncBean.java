package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AsyncCloseable;
import com.dburyak.vertx.core.AsyncInitializable;
import com.dburyak.vertx.core.di.VerticleStartup;
import io.reactivex.rxjava3.core.Completable;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@VerticleStartup(HelloVerticle2.class)
@Singleton
@Slf4j
public class SampleVerticleStartupAsyncBean implements AsyncInitializable, AsyncCloseable {

    public SampleVerticleStartupAsyncBean() {
        log.info("constructor new verticle startup async bean: this={}", this);
    }

    @Override
    public Completable initAsync() {
        return Completable.fromRunnable(() -> log.info("SampleVerticleStartupAsyncBean initialized - " +
                "async init block - called for each matching verticle instance"));
    }

    @Override
    public Completable closeAsync() {
        return Completable.fromRunnable(() -> log.info("SampleVerticleStartupAsyncBean closed - " +
                        "async close block - called for each matching verticle instance"))
                .delaySubscription(100, MILLISECONDS);
    }
}
