package com.dburyak.vertx.core.deployment;

import com.dburyak.vertx.core.deployment.spec.Deployment;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;

@Factory
@Secondary
public class DeploymentFactory {

    @Bean
    @Secondary
    public Deployment deployment() {
        // TODO: implement parsing of routing config
        return new Deployment();
    }
}
