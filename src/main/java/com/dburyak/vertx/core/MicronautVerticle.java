package com.dburyak.vertx.core;

import com.archiuse.mindis.di.VerticleBean;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Primary;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Context;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

import static io.micronaut.inject.qualifiers.Qualifiers.byQualifiers;
import static io.micronaut.inject.qualifiers.Qualifiers.byStereotype;

@Singleton
@Slf4j
@Getter
public class MicronautVerticle extends AbstractVerticle {

    private volatile Context verticleVertxCtx;
    protected volatile ApplicationContext verticleBeanCtx;

    @Override
    public final Completable rxStart() {
        verticleVertxCtx = new Context(super.context);
        return Completable
                .fromAction(() -> {
                    log.info("starting verticle: {}", this);
                    log.debug("starting verticle bean context: verticle={}, verticleCtx={}", this, verticleBeanCtx);
                    verticleBeanCtx.registerSingleton(ApplicationContext.class, verticleBeanCtx,
                            byQualifiers(byStereotype(Primary.class), byStereotype(VerticleBean.class)));
                    verticleBeanCtx.registerSingleton(this);
                    verticleBeanCtx.refreshBean(verticleBeanCtx.findBeanRegistration(verticleBeanCtx).orElseThrow()
                            .getIdentifier());
                    verticleBeanCtx.refreshBean(verticleBeanCtx.findBeanRegistration(this).orElseThrow()
                            .getIdentifier());
                })
                .andThen(Completable.defer(this::doOnStart))
                .doOnComplete(() -> log.info("verticle started: {}", this))
                .doOnError(e -> log.error("failed to start verticle: {}", this, e));
    }

    @Override
    public final Completable rxStop() {
        return Completable
                .fromAction(() -> log.info("stopping verticle: {}", this))
                .andThen(Completable.defer(this::doOnStop))
                .doOnComplete(() -> log.info("verticle stopped: {}", this))
                .doOnError(e -> log.error("failed to stop verticle: {}", this, e));
    }

    protected Completable doOnStart() {
        return Completable.complete();
    }

    protected Completable doOnStop() {
        return Completable.complete();
    }
}
