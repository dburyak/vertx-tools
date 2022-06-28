package com.dburyak.vertx.core;

import io.vertx.core.DeploymentOptions;

public interface VerticleDeploymentDescriptor {
    Class<? extends DiVerticle> verticleClass();

    DeploymentOptions deploymentOptions();
}
