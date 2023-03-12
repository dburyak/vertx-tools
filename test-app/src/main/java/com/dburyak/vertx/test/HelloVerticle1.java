package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AbstractDiVerticle;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.rxjava3.core.eventbus.EventBus;
import jakarta.inject.Inject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Bean
@Slf4j
public class HelloVerticle1 extends AbstractDiVerticle {
    @Setter(onMethod_ = {@Inject})
    private SampleEventLoopBean sampleEventLoopBean;

    @Setter(onMethod_ = {@Inject})
    private SampleVerticleBean sampleVerticleBean;

    @Setter(onMethod_ = {@Inject})
    private EventBus eventBus;

    private Disposable ticker;

    @Override
    public Completable rxStart() {
        return Completable.fromRunnable(() -> {
            log.info("hello from verticle 1: instance={}, elBean={}, vBean={}",
                    this, sampleEventLoopBean, sampleVerticleBean);
            ticker = Observable.interval(1, TimeUnit.SECONDS)
                    .doOnNext(tick -> {
                        log.info("tick verticle 1: {}", tick);
                        sampleEventLoopBean.hello();
                        sampleVerticleBean.hello();
                    })
                    .subscribe();
            Observable.interval(3, 3, TimeUnit.SECONDS)
                    .take(1)
                    .subscribe(tick -> {
                        var data = SampleDtoOne.builder()
                                .strValue("str-data")
                                .intValue(42)
                                .build();
                        eventBus.send("com.dburyak.vertx.test.HelloVerticle2", data);
                    });
        });
    }

    @Override
    public Completable rxStop() {
        return Completable.fromRunnable(() -> {
            log.info("stop verticle 1: instance={}, elBean={}, vBean={}",
                    this, sampleEventLoopBean, sampleVerticleBean);
            ticker.dispose();
        });
    }
}
