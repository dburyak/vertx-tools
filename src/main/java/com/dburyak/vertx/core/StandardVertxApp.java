package com.dburyak.vertx.core;

import java.util.List;

/**
 * Vertx application with default approach for deploying verticles - by using deployment configuration.
 * <p>
 * This implementation deploys only single verticle on startup - {@link DeployerVerticle}. Deployer verticle then
 * reads and parses deployment configuration and deploys all the verticles specified there.
 */
public class StandardVertxApp extends VertxApp {

    @Override
    public List<VerticleProducer<?>> getVerticlesProducers() {
        return List.of(new DeployerVerticle.Producer());
    }
}
