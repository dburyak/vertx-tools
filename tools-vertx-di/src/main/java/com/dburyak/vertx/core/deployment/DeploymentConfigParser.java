package com.dburyak.vertx.core.deployment;

import com.dburyak.vertx.core.deployment.spec.Deployment;

import java.io.InputStream;

public interface DeploymentConfigParser {
    boolean canParse(String deploymentConfigPath);

    Deployment parse(InputStream encodedRoutingConfig);
}
