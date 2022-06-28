package com.dburyak.vertx.core.deployer;

import com.dburyak.vertx.core.deployment.spec.Deployment;
import com.dburyak.vertx.core.di.EventLoop;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.vertx.core.Verticle;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import static io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER;

@Singleton
public class DeployerVerticle extends Verticle {
    private final VertxApp app;
    private final Deployment deployment;
    private final Scheduler eventLoop;
    private Set<String> deploymentIds;

    public DeployerVerticle(VertxApp app, Deployment deployment, @EventLoop Scheduler eventLoop) {
        this.app = app;
        this.deployment = deployment;
        this.eventLoop = eventLoop;
    }

    @Override
    protected Completable doOnStart() {
        return Flowable.fromIterable(deployment.getVerticles().getVerticles())
                .map(verticleSpec -> verticleSpec.createBySpec(deployment.getVerticles()))
                .flatMap(Flowable::fromIterable)

                // TODO: no any sense to use .parallel() here, flatMap should do parallelization, need to revisit
                // parallelize verticles deployments, no any sense to do it sequentially
                .parallel().runOn(eventLoop)
                .flatMap(p -> app.deployVerticle(p).toFlowable(BUFFER))
                .sequential()
                .toList()
                .doOnSuccess(depIds -> deploymentIds = new HashSet<>(depIds))
                .ignoreElement();
    }

    @Override
    protected Completable doOnStop() {
        return Flowable.fromIterable(deploymentIds)

                // parallelize un-deployments, no any sense to do it sequentially
                .parallel()
                .flatMap(depId -> app.undeployVerticle(depId).toFlowable())
                .sequential()
                .ignoreElements();
    }
}
