package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.AsyncCloseable;
import com.dburyak.vertx.core.di.EventLoopScope;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Bean
@EventLoopScope
@Log4j2
public class AsyncEventLoopBean implements AsyncCloseable {

    @PreDestroy
    public void closeSync() {
        log.info("AsyncEventLoopBean sync close: instance={}", this);
    }

    @Override
    public Completable closeAsync() {
        return Completable.fromRunnable(() -> log.info("AsyncEventLoopBean async close: instance={}", this))
                .delay(300, MILLISECONDS)
                .doOnComplete(() -> log.info("AsyncEventLoopBean async close completed: instance={}", this));
    }
}
