package com.dburyak.vertx.test;

import com.dburyak.vertx.core.DiVerticle;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Bean
public class HelloVerticle2 extends DiVerticle {
    private Disposable ticker;

    public HelloVerticle2(ApplicationContext appCtx) {
        super(appCtx);
    }

    @Override
    public Completable rxStart() {
        return Completable.fromRunnable(() -> {
            log.info("hello from verticle 2: instance={}", this);
            ticker = Observable.interval(1, TimeUnit.SECONDS)
                    .doOnNext(ignr -> log.info("tick verticle 2: {}", ignr))
                    .subscribe();
        });
    }

    @Override
    public Completable rxStop() {
        return Completable.fromRunnable(() -> {
            log.info("stop verticle 2: instance={}", this);
            ticker.dispose();
        });
    }
}
