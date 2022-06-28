package com.dburyak.vertx.core.deployer.deployment;

import com.dburyak.vertx.core.deployer.deployment.spec.Deployment;

import java.io.InputStream;

public interface DeploymentConfigParser {
    boolean canParse(String deploymentConfigPath);

    Deployment parse(InputStream encodedRoutingConfig);
}
