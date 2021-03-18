package com.dburyak.vertx.core.deployment;

import java.io.InputStream;

public interface DeploymentConfigParser {
    boolean canParse(String deploymentConfigPath);

    DeploymentConfigFormat parse(InputStream encodedRoutingConfig);
}
