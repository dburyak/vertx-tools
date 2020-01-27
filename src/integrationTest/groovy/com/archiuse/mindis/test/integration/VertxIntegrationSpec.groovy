package com.archiuse.mindis.test.integration


import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import static com.archiuse.mindis.test.integration.TestExecutionThread.VERTX_EL_THREAD
import static java.util.concurrent.TimeUnit.SECONDS

/**
 * Vertx app integration test specification.
 * Provides following features:
 * <ul>
 *     <li>mindis vertx app is started before running tests and is stopped afterwards
 *     <li>test verticle is deployed for specification, and all tests are being run on EL context of this verticle;
 *         and test verticle is undeployed after the spec has been run
 *     <li>
 * </ul>
 */
@VertxIntegrationTest
abstract class VertxIntegrationSpec extends Specification {

    @Shared
    IntegrationTestVerticle integrationTestVerticle = new IntegrationTestVerticle()

    @Shared
    String integrationVerticleDeploymentId

    @Shared
    VertxIntegrationApp app = VertxIntegrationApp.instance

    @Timeout(value = 5, unit = SECONDS)
    void setupSpec() {
        integrationVerticleDeploymentId = app
                .deployVerticle({ integrationTestVerticle })
                .blockingGet()

        def injection = new Async()
        integrationTestVerticle.vertxContext.runOnContext {
            def verticleCtx = integrationTestVerticle.verticleBeanCtx
            verticleCtx.registerSingleton(this)
            verticleCtx.refreshBean(verticleCtx.findBeanRegistration(this).get().identifier)
            injection.complete()
        }
        injection.await()
    }

    @Timeout(value = 5, unit = SECONDS)
    @RunOn(VERTX_EL_THREAD)
    void setup() {
        def verticleCtx = integrationTestVerticle.verticleBeanCtx
        verticleCtx.registerSingleton(this)
        verticleCtx.refreshBean(verticleCtx.findBeanRegistration(this).get().identifier)
    }

    @Timeout(value = 5, unit = SECONDS)
    void cleanupSpec() {
        app.undeployVerticle(integrationVerticleDeploymentId).blockingGet()
    }
}
