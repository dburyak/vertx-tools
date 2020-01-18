package com.archiuse.mindis

import io.micronaut.context.ApplicationContext
import io.vertx.core.DeploymentOptions

import java.util.function.Supplier

abstract class VerticleProducer {
    ApplicationContext verticleBeanCtx
    DeploymentOptions deploymentOptions = new DeploymentOptions()

    final Supplier<MindisVerticle> getVerticleSupplier() {
        return {
            def verticle = doCreateVerticle()
            verticle.verticleBeanCtx = verticleBeanCtx
            verticle
        }
    }

    abstract MindisVerticle doCreateVerticle()
}
