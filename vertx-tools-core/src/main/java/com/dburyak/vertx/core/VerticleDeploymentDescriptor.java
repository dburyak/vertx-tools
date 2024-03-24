package com.dburyak.vertx.core;

import io.vertx.core.DeploymentOptions;
import lombok.Builder;
import lombok.Value;

/**
 * Contains all the parameters for defining verticle to be deployed in declarative way.
 */
@Value
@Builder(toBuilder = true)
public class VerticleDeploymentDescriptor {
    Class<? extends AbstractDiVerticle> verticleClass;

    @Builder.Default
    DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(1);

    public static VerticleDeploymentDescriptor of(Class<? extends AbstractDiVerticle> verticleClass) {
        return VerticleDeploymentDescriptor.builder()
                .verticleClass(verticleClass)
                .build();
    }

    public static VerticleDeploymentDescriptor of(Class<? extends AbstractDiVerticle> verticleClass, int instances) {
        return VerticleDeploymentDescriptor.builder()
                .verticleClass(verticleClass)
                .deploymentOptions(new DeploymentOptions().setInstances(instances))
                .build();
    }
}
