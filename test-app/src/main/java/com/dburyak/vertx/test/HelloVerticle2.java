package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AbstractDiVerticle;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.core.eventbus.Message;
import io.vertx.rxjava3.core.eventbus.MessageConsumer;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Bean
@Slf4j
public class HelloVerticle2 extends AbstractDiVerticle {
    private SampleEventLoopBean sampleEventLoopBean;
    private SampleVerticleBean sampleVerticleBean;
    private EventBus eventBus;

    private Disposable ticker;
    private MessageConsumer echoMsgConsumer;

    @Override
    public Completable startup() {
        return Completable.fromRunnable(() -> {
            log.info("hello from verticle 2: instance={}, elBean={}, vBean={}",
                    this, sampleEventLoopBean, sampleVerticleBean);
            ticker = Observable.interval(4, TimeUnit.SECONDS)
                    .doOnNext(tick -> {
                        log.info("tick verticle 2: {}", tick);
                        sampleEventLoopBean.hello();
                        sampleVerticleBean.hello();
                    })
                    .subscribe();
            echoMsgConsumer = eventBus.consumer("com.dburyak.vertx.test.HelloVerticle2", this::echo);
            new Thread(() -> {
                var bean = appCtx.getBean(SampleThreadLocalBean.class);
                log.info("thread local bean injected: bean={}", bean);
            }).start();
        });
    }

    @Override
    public Completable shutdown() {
        return Completable.fromRunnable(() -> {
            log.info("stop verticle 2: instance={}, elBean={}, vBean={}",
                    this, sampleEventLoopBean, sampleVerticleBean);
            ticker.dispose();
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

    private void echo(Message<Object> msg) {
        log.info("got echo message: {}", msg.body());
    }
}
