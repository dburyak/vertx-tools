package com.dburyak.vertx.core


import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.reactivex.Single
import io.vertx.core.DeploymentOptions
import io.vertx.reactivex.core.Vertx
import spock.lang.Specification
import spock.lang.Timeout

import static com.dburyak.vertx.core.VertxApp.PROP_IS_APP_BEAN_CTX
import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 5, unit = SECONDS)
class VertxAppSpec extends Specification {
    VertxApp app = Spy(VertxApp)

    Vertx vertx = Mock(Vertx)
    ApplicationContextBuilder mainAppCtxBuilder = Mock(ApplicationContextBuilder)
    ApplicationContext mainAppCtx = Mock(ApplicationContext)
    VerticleProducer initVerticleProducer = Mock(VerticleProducer)
    ApplicationContextBuilder initVerticleCtxBuilder = Mock(ApplicationContextBuilder)
    ApplicationContext initVerticleCtx = Mock(ApplicationContext)

    def 'start app - main flow'() {
        given: 'not started vertx application'
        def initVerticleOpts = new DeploymentOptions()
        def initVerticleDepId = 'init-verticle-dep-id-uuid'

        when: 'start app'
        def res = app.start().test().await()

        then: 'app is started'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()

        and:
        interaction appIsStartedAndInitVerticleDeployed(initVerticleOpts, initVerticleDepId)
    }

    def 'start app - throws exception if it is already running'() {
        given: 'running application with initialized bean context'

        when: 'start already running application again'
        def startResult = app.start()
                .andThen(app.start())
                .test().await()

        then: 'exception is thrown in async chain'
        noExceptionThrown()
        startResult.assertError(IllegalStateException)
        startResult.assertError { it.message.startsWith 'application is already running' }

        and: 'app was started first time'
        interaction appIsStarted()

        and: 'app was not started second time'
        0 * app.newApplicationContextBuilder()
    }

    def appIsStarted() {
        return {
            and: 'main app ctx is built started'
            1 * app.newApplicationContextBuilder() >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.properties(Map.of(PROP_IS_APP_BEAN_CTX, true)) >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.start() >> mainAppCtx

            and: 'main app ctx is set'
            app.mainApplicationContext == mainAppCtx
        }
    }

    def appIsStartedAndInitVerticleDeployed(DeploymentOptions initVerticleOpts, String initVerticleDepId) {
        return {
            // and: 'main app ctx is built started'
            1 * app.newApplicationContextBuilder() >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.properties(Map.of(PROP_IS_APP_BEAN_CTX, true)) >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.start() >> mainAppCtx

            // and: 'main app ctx is set'
            app.mainApplicationContext == mainAppCtx

            // and: 'init verticle producer is called'
            1 * app.getVerticlesProducers() >> List.of(initVerticleProducer)
            2 * initVerticleProducer.getDeploymentOptions() >> initVerticleOpts

            // and: 'init verticle ctx is built and configured'
            1 * app.newApplicationContextBuilder() >> initVerticleCtxBuilder
            1 * initVerticleCtxBuilder.properties(Map.of(PROP_IS_APP_BEAN_CTX, false)) >> initVerticleCtxBuilder
            1 * initVerticleCtxBuilder.start() >> initVerticleCtx

            // and: 'global beans are injected into initVerticle ctx'
            1 * initVerticleCtx.registerSingleton(ApplicationContext.class, mainAppCtx, _) >> initVerticleCtx
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * initVerticleCtx.registerSingleton(Vertx.class, vertx, _) >> initVerticleCtx

            // and: 'init verticle is deployed'
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * initVerticleProducer.setVerticleBeanCtx(initVerticleCtx)
            1 * vertx.rxDeployVerticle(initVerticleProducer, initVerticleOpts) >> Single.just(initVerticleDepId)

            // and: 'init verticle bean ctx is registered'
            app.getVerticleApplicationContext(initVerticleDepId) == initVerticleCtx
        }
    }
}
