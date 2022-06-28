package com.dburyak.vertx.core.deployer.deployment;

import com.dburyak.vertx.core.deployer.deployment.spec.Deployment;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Value;

import java.util.List;

@Factory
@Secondary
public class DeploymentFactory {

    @Bean
    @Secondary
    public Deployment deployment(@Value("${application.deployment.config:deployment.groovy}") String deploymentFilePath,
            List<DeploymentConfigParser> parsers) {
        // TODO: implement parsing of routing config
        return Deployment.builder().build();
    }

    private DeploymentConfigParser findParser(String deploymentFilePath, List<DeploymentConfigParser> parsers) {
        return parsers.stream()
                .filter(p -> p.canParse(deploymentFilePath))
                .findFirst().orElseThrow();
    }
}
