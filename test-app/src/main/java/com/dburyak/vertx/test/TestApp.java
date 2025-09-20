package com.dburyak.vertx.test;

import com.dburyak.vertx.core.VerticleDeploymentDescriptor;
import com.dburyak.vertx.core.VertxDiApp;
import io.vertx.core.DeploymentOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class TestApp extends VertxDiApp {

    public static void main(String[] args) {
        log.info("starting ....");
        var app = new TestApp();
        app.start()
                .delay(60, SECONDS)
                .andThen(app.stop())
                .blockingAwait();
    }

    @Override
    protected Collection<VerticleDeploymentDescriptor> verticlesDeploymentDescriptors() {
        return List.of(
                VerticleDeploymentDescriptor.builder()
                        .verticleClass(HelloVerticle1.class)
                        .build(),
                VerticleDeploymentDescriptor.builder()
                        .verticleClass(HelloVerticle2.class)
                        .deploymentOptions(new DeploymentOptions().setInstances(7))
                        .build(),
                VerticleDeploymentDescriptor.builder()
                        .verticleClass(PubSubVerticle1.class)
                        .build(),
                VerticleDeploymentDescriptor.builder()
                        .verticleClass(PubSubVerticle2.class)
                        .deploymentOptions(new DeploymentOptions().setInstances(2))
                        .build(),
                VerticleDeploymentDescriptor.builder()
                        .verticleClass(CfgConsumerVerticle.class)
                        .deploymentOptions(new DeploymentOptions().setInstances(1))
                        .build()
        );
    }
}
