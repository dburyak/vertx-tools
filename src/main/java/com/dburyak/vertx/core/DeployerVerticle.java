package com.dburyak.vertx.core;

import com.dburyak.vertx.core.deployment.spec.Deployment;
import com.dburyak.vertx.core.di.EventLoop;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.reactivex.BackpressureStrategy.BUFFER;

@Singleton
@Setter(onMethod_ = {@Inject})
public class DeployerVerticle extends Verticle {
    private VertxApp app;
    private Deployment deployment;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@EventLoop})
    private Scheduler eventLoop;

    @Override
    protected Completable doOnStart() {
        return Flowable.fromIterable(deployment.getVerticles().getVerticles())
                .map(verticleSpec -> verticleSpec.createBySpec(deployment.getVerticles()))
                .flatMap(Flowable::fromIterable)

                // parallelize verticles deployments, no any sense to do it sequentially
                .parallel().runOn(eventLoop)
                .flatMap(p -> app.deployVerticle(p).toFlowable(BUFFER))
                .sequential()
                .ignoreElements();
    }

    public static class Producer extends VerticleProducer<Producer> {

        @Override
        protected Verticle doCreateVerticle() {
            return new DeployerVerticle();
        }
    }
}
