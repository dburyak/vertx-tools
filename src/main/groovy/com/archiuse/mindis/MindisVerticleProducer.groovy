package com.archiuse.mindis

import io.micronaut.context.ApplicationContext
import io.vertx.core.DeploymentOptions

import java.util.function.Supplier

abstract class MindisVerticleProducer {
    ApplicationContext verticleBeanCtx
    DeploymentOptions deploymentOptions = new DeploymentOptions()

    final Supplier<MindisVerticle> getVerticleProducer() {
        return {
            def verticle = doCreateVerticle()
            verticle.verticleBeanCtx = verticleBeanCtx
            verticle
        }
    }

    abstract MindisVerticle doCreateVerticle()
}
