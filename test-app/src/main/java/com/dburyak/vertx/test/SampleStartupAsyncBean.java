package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AsyncInitializable;
import com.dburyak.vertx.core.di.AppStartup;
import io.reactivex.rxjava3.core.Completable;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@AppStartup
@Singleton
@Slf4j
public class SampleStartupAsyncBean implements AsyncInitializable {

    @Override
    public Completable initAsync() {
        return Completable.fromRunnable(() -> log.info("SampleAsyncStartupBean initialized"))
                .delaySubscription(300, MILLISECONDS);
    }
}
