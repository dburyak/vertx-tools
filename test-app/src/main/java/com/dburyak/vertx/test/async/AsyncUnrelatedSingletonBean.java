package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.AsyncCloseable;
import io.reactivex.rxjava3.core.Completable;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.extern.log4j.Log4j2;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Singleton
@Log4j2
public class AsyncUnrelatedSingletonBean implements AsyncCloseable {

    @PreDestroy
    public void closeSync() {
        log.info("closing AsyncUnrelatedSingletonBean sync: instance={}", this);
    }

    @Override
    public Completable closeAsync() {
        return Completable.fromRunnable(() -> log.info("closing AsyncUnrelatedSingletonBean async: instance={}", this))
                .delay(200, MILLISECONDS);
    }
}
