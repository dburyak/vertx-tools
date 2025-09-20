package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.AsyncCloseable;
import com.dburyak.vertx.core.di.VerticleScope;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Bean
@VerticleScope
@Log4j2
public class AsyncVerticleBean implements AsyncCloseable {

    @PreDestroy
    public void closeSync() {
        log.info("AsyncVerticleBean sync close: instance={}", this);
    }

    @Override
    public Completable closeAsync() {
        return Completable.fromRunnable(() -> log.info("AsyncVerticleBean async close: instance={}", this))
                .delay(500, MILLISECONDS);
    }
}
