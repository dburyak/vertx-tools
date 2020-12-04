package com.dburyak.vertx.core;

import io.micronaut.context.ApplicationContext;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.function.Supplier;

@Getter
@Setter
public abstract class MicronautVerticleProducer<I extends MicronautVerticleProducer<I>> implements Supplier<Verticle> {
    private String name = getClass().getCanonicalName();
    private ApplicationContext verticleBeanCtx;
    private DeploymentOptions deploymentOptions = new DeploymentOptions();

    @Override
    public final MicronautVerticle get() {
        var verticle = doCreateVerticle();
        if (verticleBeanCtx == null) {
            throw new IllegalStateException("target verticle bean ctx must be specified for micronaut verticle");
        }
        verticle.verticleBeanCtx = verticleBeanCtx;
        return verticle;
    }

    protected abstract MicronautVerticle doCreateVerticle();

    public I withName(String name) {
        setName(name);
        return (I) this;
    }

    public void setVerticleBeanCtx(ApplicationContext verticleBeanCtx) {
        Objects.requireNonNull(verticleBeanCtx);
        this.verticleBeanCtx = verticleBeanCtx;
    }

    public I withVerticleBeanCtx(ApplicationContext verticleBeanCtx) {
        setVerticleBeanCtx(verticleBeanCtx);
        return (I) this;
    }

    public void setDeploymentOptions(DeploymentOptions deploymentOptions) {
        Objects.requireNonNull(deploymentOptions);
        this.deploymentOptions = deploymentOptions;
    }

    public I withDeploymentOptions(DeploymentOptions deploymentOptions) {
        setDeploymentOptions(deploymentOptions);
        return (I) this;
    }
}
