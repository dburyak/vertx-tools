package com.dburyak.vertx.core.deployment;

public interface DeploymentConfigParser {
    boolean canParse(DeploymentConfigFormat format);

    DeploymentConfigFormat parse(Object encodedRoutingConfig);
}
