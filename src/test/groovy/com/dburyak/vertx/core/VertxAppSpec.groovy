package com.dburyak.vertx.core


import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.core.DeploymentOptions
import io.vertx.reactivex.core.Vertx
import spock.lang.Specification
import spock.lang.Timeout

import static com.dburyak.vertx.core.AppState.FAILED
import static com.dburyak.vertx.core.AppState.RUNNING
import static com.dburyak.vertx.core.AppState.STOPPED
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

    def 'start - starts successfully'() {
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

        and: 'main app ctx is set'
        app.mainApplicationContext == mainAppCtx

        and: 'init verticle bean ctx is registered'
        app.getVerticleApplicationContext(initVerticleDepId) == initVerticleCtx

        and: 'app state is RUNNING'
        app.appState == RUNNING
    }

    def 'start - throws exception if app is already running'() {
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

    def 'stop - stops successfully'() {
        given: 'running app'
        def initVerticleOpts = new DeploymentOptions()
        def initVerticleDepId = 'init-verticle-dep-id-uuid'

        when: 'stop running app'
        def res = app.start()
                .andThen(app.stop())
                .test().await()

        then: 'app is stopped'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()

        and:
        interaction appIsStartedAndInitVerticleDeployed(initVerticleOpts, initVerticleDepId)

        and:
        interaction appAndInitVerticleIsStopped()

        and: 'application state is STOPPED'
        app.appState == STOPPED
    }

    def 'stop - throws exception if app is not running'() {
        given: 'not running app'

        when: 'stop not running app'
        def stopResult = app.stop().test().await()

        then: 'exception is thrown in async chain'
        noExceptionThrown()
        stopResult.assertError(IllegalStateException)
        stopResult.assertError { it.message.startsWith 'application is not running' }

        and:
        app.appState == FAILED
    }

    def 'stop - throws exception if app was stopped'() {
        given: 'app that was started and stopped'

        when: 'stop app one more time'
        def res = app.start()
                .andThen(app.stop())
                .andThen(app.stop())
                .test().await()

        then: 'exception is thrown in async chain'
        noExceptionThrown()
        res.assertError(IllegalStateException)
        res.assertError { it.message.startsWith 'application is not running' }
        res.assertError { it.message.contains(STOPPED as String) }

        and:
        interaction appIsStarted()
        interaction appIstStopped()

        and:
        app.appState == FAILED
    }

    def 'deployVerticle - deploys verticle successfully'() {
        given: 'running app'
        def initVerticleOpts = new DeploymentOptions()
        def initVerticleDepId = 'init-verticle-dep-id-uuid'

        when: 'deploy verticle'
        def res = app.start()
                .andThen(app.deployVerticle(initVerticleProducer))
                .test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()

        and:
        interaction appIsStarted()

        and:
        interaction initVerticleDeployed(initVerticleOpts, initVerticleDepId)

        and:
        app.getVerticleApplicationContext(initVerticleDepId) == initVerticleCtx
    }

    private def initVerticleDeployed(DeploymentOptions initVerticleOpts, String initVerticleDepId) {
        return {
            2 * initVerticleProducer.getDeploymentOptions() >> initVerticleOpts
            1 * app.newApplicationContextBuilder() >> initVerticleCtxBuilder
            1 * initVerticleCtxBuilder.properties(Map.of(PROP_IS_APP_BEAN_CTX, false)) >> initVerticleCtxBuilder
            1 * initVerticleCtxBuilder.start() >> initVerticleCtx
            1 * initVerticleCtx.registerSingleton(ApplicationContext.class, mainAppCtx, _)
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * initVerticleCtx.registerSingleton(Vertx.class, vertx, _)
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * initVerticleProducer.setVerticleBeanCtx(initVerticleCtx)
            1 * vertx.rxDeployVerticle(initVerticleProducer, initVerticleOpts) >> Single.just(initVerticleDepId)
        }
    }

    private def appIsStarted() {
        return {
            and: 'main app ctx is built started'
            1 * app.newApplicationContextBuilder() >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.properties(Map.of(PROP_IS_APP_BEAN_CTX, true)) >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.start() >> mainAppCtx
        }
    }

    private def appIsStartedAndInitVerticleDeployed(DeploymentOptions initVerticleOpts, String initVerticleDepId) {
        return {
            and: 'main app ctx is built started'
            1 * app.newApplicationContextBuilder() >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.properties(Map.of(PROP_IS_APP_BEAN_CTX, true)) >> mainAppCtxBuilder
            1 * mainAppCtxBuilder.start() >> mainAppCtx

            and: 'init verticle producer is called'
            1 * app.getVerticlesProducers() >> List.of(initVerticleProducer)
            2 * initVerticleProducer.getDeploymentOptions() >> initVerticleOpts

            and: 'init verticle ctx is built and configured'
            1 * app.newApplicationContextBuilder() >> initVerticleCtxBuilder
            1 * initVerticleCtxBuilder.properties(Map.of(PROP_IS_APP_BEAN_CTX, false)) >> initVerticleCtxBuilder
            1 * initVerticleCtxBuilder.start() >> initVerticleCtx

            and: 'global beans are injected into initVerticle ctx'
            1 * initVerticleCtx.registerSingleton(ApplicationContext.class, mainAppCtx, _) >> initVerticleCtx
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * initVerticleCtx.registerSingleton(Vertx.class, vertx, _) >> initVerticleCtx

            and: 'init verticle is deployed'
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * initVerticleProducer.setVerticleBeanCtx(initVerticleCtx)
            1 * vertx.rxDeployVerticle(initVerticleProducer, initVerticleOpts) >> Single.just(initVerticleDepId)
        }
    }

    private def appAndInitVerticleIsStopped() {
        return {
            and: 'vertx is closed'
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * vertx.rxClose() >> Completable.complete()

            and: 'init verticle bean ctx is closed'
            1 * initVerticleCtx.stop()

            and: 'main app bean ctx is closed'
            1 * mainAppCtx.stop()
        }
    }

    private def appIstStopped() {
        return {
            and: 'vertx is closed'
            1 * mainAppCtx.getBean(Vertx.class) >> vertx
            1 * vertx.rxClose() >> Completable.complete()

            and: 'main app bean ctx is closed'
            1 * mainAppCtx.stop()
        }
    }
}
