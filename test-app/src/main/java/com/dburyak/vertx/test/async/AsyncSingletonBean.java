package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.AsyncCloseable;
import com.dburyak.vertx.core.AsyncInitializable;
import io.reactivex.rxjava3.core.Completable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.extern.log4j.Log4j2;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Singleton
@Log4j2
public class AsyncSingletonBean implements AsyncCloseable {

    @PreDestroy
    public void closeSync() {
        log.info("AsyncSingletonBean sync close: instance={}", this);
    }

    @Override
    public Completable closeAsync() {
        return Completable.fromRunnable(() -> log.info("AsyncSingletonBean async close: instance={}", this))
                .delay(100, MILLISECONDS)
                .doOnComplete(() -> log.info("AsyncSingletonBean async close completed: instance={}", this));
    }
}
