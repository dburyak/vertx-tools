package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.AsyncInitializable;
import com.dburyak.vertx.core.di.VerticleScope;
import com.dburyak.vertx.core.di.VerticleStartup;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Bean
@VerticleStartup(AsyncVerticle.class)
@VerticleScope
@Log4j2
public class AsyncVerticleStartup implements AsyncInitializable {

    @PostConstruct
    public void initSync() {
        log.info("AsyncVerticleStartup sync init: instance={}", this);
    }

    @Override
    public Completable initAsync() {
        return Completable.fromRunnable(() -> log.info("AsyncVerticleStartup async init: instance={}", this))
                .delaySubscription(1_000, MILLISECONDS);
    }
}
