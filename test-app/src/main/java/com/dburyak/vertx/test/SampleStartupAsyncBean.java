package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AsyncAction;
import com.dburyak.vertx.core.di.AppStartup;
import io.reactivex.rxjava3.core.Completable;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@AppStartup
@Singleton
@Slf4j
public class SampleStartupAsyncBean implements AsyncAction {

    @Override
    public Completable execute() {
        return Completable.fromRunnable(() -> log.info("SampleAsyncStartupBean initialized"));
    }
}
