package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.VerticleDeploymentDescriptor;
import com.dburyak.vertx.core.VertxDiApp;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.List;

@Log4j2
public class AsyncLifecycleBeansApp extends VertxDiApp {

    public static void main(String[] args) throws InterruptedException {
        var app = new AsyncLifecycleBeansApp();
        app.start().blockingAwait();
        Thread.sleep(7_000);
        app.stop().blockingAwait();
        log.info("AsyncLifecycleBeansApp stopped");
    }

    @Override
    protected Collection<VerticleDeploymentDescriptor> verticlesDeploymentDescriptors() {
        return List.of(VerticleDeploymentDescriptor.of(AsyncVerticle.class, 2));
    }
}
