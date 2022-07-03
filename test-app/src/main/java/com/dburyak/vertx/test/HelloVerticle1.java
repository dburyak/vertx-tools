package com.dburyak.vertx.test;

import com.dburyak.vertx.core.DiVerticle;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import jakarta.inject.Inject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Bean
@Slf4j
public class HelloVerticle1 extends DiVerticle {
    @Setter(onMethod_ = {@Inject})
    private SampleEventLoopBean sampleEventLoopBean;
    private Disposable ticker;

    @Override
    public Completable rxStart() {
        return Completable.fromRunnable(() -> {
            log.info("hello from verticle 1: instance={}, bean={}", this, sampleEventLoopBean);
            ticker = Observable.interval(1, TimeUnit.SECONDS)
                    .doOnNext(ignr -> log.info("tick verticle 1: {}", ignr))
                    .doOnNext(ignr -> sampleEventLoopBean.hello())
                    .subscribe();
        });
    }

    @Override
    public Completable rxStop() {
        return Completable.fromRunnable(() -> {
            log.info("stop verticle 1: instance={}, bean={}", this, sampleEventLoopBean);
            ticker.dispose();
        });
    }
}
