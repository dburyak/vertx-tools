package com.dburyak.vertx.core;

import io.vertx.core.DeploymentOptions;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class VerticleDeploymentDescriptor {
    Class<? extends AbstractDiVerticle> verticleClass;

    @Builder.Default
    DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(1);
}
