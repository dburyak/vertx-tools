package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AbstractDiVerticle;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.eventbus.EventBus;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Bean
@Slf4j
public class HelloVerticle1 extends AbstractDiVerticle {
    private SampleEventLoopBean sampleEventLoopBean;
    private SampleVerticleBean sampleVerticleBean;
    private EventBus eventBus;
    private ConfigRetriever configRetriever;

    private Disposable ticker;
    private Disposable configPoll;

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
            configPoll = configRetriever.rxGetConfig()
                    .subscribe(cfgJson -> log.debug("config retrieved: {}", cfgJson));
        });
    }

    @Override
    public Completable rxStop() {
        return Completable.fromRunnable(() -> {
            log.info("stop verticle 1: instance={}, elBean={}, vBean={}",
                    this, sampleEventLoopBean, sampleVerticleBean);
            ticker.dispose();
            configPoll.dispose();
        });
    }

    @Inject
    public void setSampleEventLoopBean(SampleEventLoopBean sampleEventLoopBean) {
        this.sampleEventLoopBean = sampleEventLoopBean;
    }

    @Inject
    public void setSampleVerticleBean(SampleVerticleBean sampleVerticleBean) {
        this.sampleVerticleBean = sampleVerticleBean;
    }

    @Inject
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Inject
    public void setConfigRetriever(ConfigRetriever configRetriever) {
        this.configRetriever = configRetriever;
    }
}
