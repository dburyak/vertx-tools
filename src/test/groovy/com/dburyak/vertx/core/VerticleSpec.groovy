package com.dburyak.vertx.core

import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanRegistration
import io.micronaut.inject.BeanIdentifier
import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.core.Context
import io.vertx.ext.healthchecks.CheckResult
import io.vertx.ext.healthchecks.Status
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.file.FileSystem
import io.vertx.reactivex.ext.healthchecks.HealthChecks
import spock.lang.Timeout

import java.time.Instant
import java.time.ZoneId

import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 20, unit = SECONDS)
class VerticleSpec extends BaseVertxRxJavaSpec {
    Verticle verticle = Spy(Verticle)

    FileSystem fs = Mock(FileSystem)
    Context verticleVertxCtx = Mock(Context)
    ApplicationContext verticleBeanCtx = Mock(ApplicationContext)
    BeanRegistration beanCtxBeanReg = Mock(BeanRegistration)
    BeanRegistration verticleBeanReg = Mock(BeanRegistration)
    BeanIdentifier verticleBeanId = Mock(BeanIdentifier)
    BeanIdentifier beanCtxBeanId = Mock(BeanIdentifier)
    HealthChecks healthChecks = Mock(HealthChecks)
    HealthChecks readyChecks = Mock(HealthChecks)

    void setup() {
        verticle.@vertx = vertx
        verticle.@context = verticleVertxCtx
        verticle.@verticleBeanCtx = verticleBeanCtx
        verticle.fs = fs
        verticle.healthChecks = healthChecks
        verticle.readyChecks = readyChecks
    }

    def 'rxStart inits bean context and calls doOnStart hook'() {
        when: 'call rxStart'
        def res = verticle.rxStart().test().await()

        then: 'verticle started successfully'
        noExceptionThrown()
        res.assertNoErrors()

        and: 'verticle bean context and verticle itself are registered within DI'
        1 * verticleBeanCtx.registerSingleton(ApplicationContext, verticleBeanCtx, _) >> verticleBeanCtx
        1 * verticleBeanCtx.registerSingleton(verticle)

        and: 'verticle bean context is refreshed within DI container'
        1 * verticleBeanCtx.findBeanRegistration(verticleBeanCtx) >> Optional.of(beanCtxBeanReg)
        1 * beanCtxBeanReg.identifier >> beanCtxBeanId
        1 * verticleBeanCtx.refreshBean(beanCtxBeanId)

        and: 'verticle bean is refreshed within DI container - results in all the verticle dependencies being injected'
        1 * verticleBeanCtx.findBeanRegistration(verticle) >> Optional.of(verticleBeanReg)
        1 * verticleBeanReg.identifier >> verticleBeanId
        1 * verticleBeanCtx.refreshBean(verticleBeanId)

        and: 'doOnStart hook is called'
        1 * verticle.doOnStart() >> Completable.complete()

        and: 'doOnStop hook is NOT called'
        0 * verticle.doOnStop()
    }

    def 'rxStop calls doOnStop hook'() {
        when: 'call rxStop'
        verticle.rxStop().test().await()

        then: 'doOnStop hook is called'
        noExceptionThrown()
        1 * verticle.doOnStop() >> Completable.complete()

        and: 'doOnStart hook is NOT called'
        0 * verticle.doOnStart()
    }

    def 'doOnStart failure results in async exception of all the start chain'() {
        when: 'try to start failing verticle'
        def res = verticle.rxStart().test().await()

        then: 'no exception is thrown outside the async chain'
        noExceptionThrown()

        and: 'exception is rather passed down the async chain'
        res.assertError { err ->
            err instanceof IllegalArgumentException
                    && err.message == 'thrown from hook'
        }

        and: 'startup hook was called and threw an error'
        1 * verticle.doOnStart() >> Completable.error(new IllegalArgumentException('thrown from hook'))

        and: 'bean context was set up'
        1 * verticleBeanCtx.registerSingleton(ApplicationContext, verticleBeanCtx, _) >> verticleBeanCtx
        1 * verticleBeanCtx.registerSingleton(verticle)

        1 * verticleBeanCtx.findBeanRegistration(verticleBeanCtx) >> Optional.of(beanCtxBeanReg)
        1 * beanCtxBeanReg.identifier >> beanCtxBeanId
        1 * verticleBeanCtx.refreshBean(beanCtxBeanId)

        1 * verticleBeanCtx.findBeanRegistration(verticle) >> Optional.of(verticleBeanReg)
        1 * verticleBeanReg.identifier >> verticleBeanId
        1 * verticleBeanCtx.refreshBean(verticleBeanId)
    }

    def 'doOnStop failure results in async exception of all the stop chain'() {
        when:
        def res = verticle.rxStop().test().await()

        then:
        noExceptionThrown()
        res.assertError { it instanceof IllegalArgumentException }
        res.assertError { it.message == 'thrown from hook' }

        and:
        1 * verticle.doOnStop() >> Completable.error(new IllegalArgumentException('thrown from hook'))
    }

    def 'about returns correct information'() {
        when: 'start verticle and get its about information'
        def res = verticle.about().test().await()

        then:
        noExceptionThrown()

        and:
        res.assertNoErrors()
        res.assertValue { it.version == '1.0' }
        res.assertValue { it.revision == 'abcdef0123456789' }
        res.assertValue { it.built_at < Instant.now() }
        res.assertValue {
            Instant.now().minusSeconds(10) < it.server_time && it.server_time < Instant.now()
        }
        res.assertValue { it.timezone == ZoneId.systemDefault().id }
        res.assertValue { it.verticle.deployment_id == 'deploymentId-uuid' }
        res.assertValue { it.verticle.type == 'EventLoop' }
        res.assertValue { it.verticle.thread.name }
        res.assertValue { it.verticle.thread.id }
        res.assertValue { it.verticle.thread.containsKey('vertx_thread') }
        res.assertValue { it.verticle.thread.containsKey('event_loop_thread') }
        res.assertValue { it.verticle.thread.containsKey('worker_thread') }
        res.assertValue { it.verticle.containsKey('config') }

        and:
        2 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * fs.rxReadFile('version.txt') >> Single.just(Buffer.buffer('1.0'))
        1 * fs.rxReadFile('revision.txt') >> Single.just(Buffer.buffer('abcdef0123456789'))
        1 * fs.rxReadFile('built_at.txt') >> Single.just(Buffer.buffer('2020-12-18T22:28:05.165893419Z'))
        1 * verticleVertxCtx.isEventLoopContext() >> true
    }

    def 'health calls hook method'() {
        given: 'no custom health checks defined'

        when: 'assess health of the verticle'
        def res = verticle
                .rxStart()
                .andThen(verticle.health())
                .test().await()

        then: 'hook method for subclasses is called'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { it.up }
        1 * verticle.registerHealthProcedures(healthChecks)

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * healthChecks.rxCheckStatus() >> Single.just(CheckResult.from(null, Status.OK()))

        and:
        1 * verticleBeanCtx.registerSingleton(ApplicationContext, verticleBeanCtx, _) >> verticleBeanCtx
        1 * verticleBeanCtx.registerSingleton(verticle)
        1 * verticleBeanCtx.findBeanRegistration(verticleBeanCtx) >> Optional.of(beanCtxBeanReg)
        1 * beanCtxBeanReg.identifier >> beanCtxBeanId
        1 * verticleBeanCtx.refreshBean(beanCtxBeanId)
        1 * verticleBeanCtx.findBeanRegistration(verticle) >> Optional.of(verticleBeanReg)
        1 * verticleBeanReg.identifier >> verticleBeanId
        1 * verticleBeanCtx.refreshBean(verticleBeanId)
        1 * verticle.doOnStart() >> Completable.complete()
    }

    def 'health check fails for not deployed verticle'() {
        given: 'not deployed verticle'

        when: 'assess health of not deployed verticle'
        def res = verticle.health().test().await()

        then: 'IllegalStateException is thrown'
        noExceptionThrown()
        res.assertError { it instanceof IllegalStateException }
        res.assertError { it.message == 'can not assess health of a not deployed verticle' }

        and:
        1 * verticleVertxCtx.deploymentID() >> null
        0 * healthChecks.rxCheckStatus()
    }

    def 'health is OK by default'() {
        given: 'no custom health checks defined'
        verticle.healthChecks = HealthChecks.create(vertx)

        when: 'assess health of the verticle'
        def res = verticle.health().test().await()

        then: 'health of the verticle is OK'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { it.up }

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
    }

    def 'health is OK when custom checks are OK'() {
        given: 'several health checks defined that all return OK'
        def realHealthChecks = HealthChecks.create(vertx)
        verticle.healthChecks = realHealthChecks

        when: 'access health of the verticle'
        verticle.registerHealthProcedures(realHealthChecks)
        def res = verticle.health().test().await()

        then: 'health of the verticle is OK'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { it.up }

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * verticle.registerHealthProcedures(realHealthChecks) >> { HealthChecks checks ->
            checks.register('one') { it.complete(Status.OK()) }
            checks.register('two') { it.complete(Status.OK()) }
            checks.register('three') { it.complete(Status.OK()) }
        }
    }

    def 'health fails when custom check fails'() {
        given: 'one of several health checks is failing'
        def realHealthChecks = HealthChecks.create(vertx)
        verticle.healthChecks = realHealthChecks

        when: 'assess health of the verticle'
        verticle.registerHealthProcedures(realHealthChecks)
        def res = verticle.health().test().await()

        then: 'health of the verticle is failing'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { !it.up }

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * verticle.registerHealthProcedures(realHealthChecks) >> { HealthChecks checks ->
            checks.register('one') { it.complete(Status.OK()) }
            checks.register('failing') { it.complete(Status.KO()) }
            checks.register('three') { it.complete(Status.OK()) }
        }
    }

    def 'readiness check calls hook method'() {
        given: 'no custom ready checks defined'

        when: 'assess readiness of the verticle'
        def res = verticle
                .rxStart()
                .andThen(verticle.ready())
                .test().await()

        then: 'hook method for subclasses is called'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { it.up }
        1 * verticle.registerReadyProcedures(readyChecks)

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * readyChecks.rxCheckStatus() >> Single.just(CheckResult.from(null, Status.OK()))

        and:
        1 * verticleBeanCtx.registerSingleton(ApplicationContext, verticleBeanCtx, _) >> verticleBeanCtx
        1 * verticleBeanCtx.registerSingleton(verticle)
        1 * verticleBeanCtx.findBeanRegistration(verticleBeanCtx) >> Optional.of(beanCtxBeanReg)
        1 * beanCtxBeanReg.identifier >> beanCtxBeanId
        1 * verticleBeanCtx.refreshBean(beanCtxBeanId)
        1 * verticleBeanCtx.findBeanRegistration(verticle) >> Optional.of(verticleBeanReg)
        1 * verticleBeanReg.identifier >> verticleBeanId
        1 * verticleBeanCtx.refreshBean(verticleBeanId)
        1 * verticle.doOnStart() >> Completable.complete()
    }

    def 'readiness check fails for not deployed verticle'() {
        given: 'not deployed verticle'

        when: 'assess readiness of not deployed verticle'
        def res = verticle.ready().test().await()

        then: 'IllegalStateException is thrown'
        noExceptionThrown()
        res.assertError { it instanceof IllegalStateException }
        res.assertError { it.message == 'can not assess readiness of a not deployed verticle' }

        and:
        1 * verticleVertxCtx.deploymentID() >> null
        0 * readyChecks.rxCheckStatus()
    }

    def 'readiness is OK by default'() {
        given: 'no custom readiness checks defined'
        verticle.readyChecks = HealthChecks.create(vertx)

        when: 'assess readiness of the verticle'
        def res = verticle.ready().test().await()

        then: 'readiness of the verticle is OK'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { it.up }

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
    }

    def 'readiness is OK when custom checks are OK'() {
        given: 'several readiness checks defined that all return OK'
        def realReadyChecks = HealthChecks.create(vertx)
        verticle.readyChecks = realReadyChecks

        when: 'access readiness of the verticle'
        verticle.registerReadyProcedures(realReadyChecks)
        def res = verticle.ready().test().await()

        then: 'readiness of the verticle is OK'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { it.up }

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * verticle.registerReadyProcedures(realReadyChecks) >> { HealthChecks checks ->
            checks.register('one') { it.complete(Status.OK()) }
            checks.register('two') { it.complete(Status.OK()) }
            checks.register('three') { it.complete(Status.OK()) }
        }
    }

    def 'readiness fails when custom check fails'() {
        given: 'one of several readiness checks is failing'
        def realReadyChecks = HealthChecks.create(vertx)
        verticle.readyChecks = realReadyChecks

        when: 'assess readiness of the verticle'
        verticle.registerReadyProcedures(realReadyChecks)
        def res = verticle.ready().test().await()

        then: 'readiness of the verticle is failing'
        noExceptionThrown()
        res.assertNoErrors()
        res.assertValue { !it.up }

        and:
        1 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * verticle.registerReadyProcedures(realReadyChecks) >> { HealthChecks checks ->
            checks.register('one') { it.complete(Status.OK()) }
            checks.register('failing') { it.complete(Status.KO()) }
            checks.register('three') { it.complete(Status.OK()) }
        }
    }

    def 'verticle deployment status: isCtxNull=#isCtxNull, deploymentId=#depId'() {
        setup:
        verticle.@context = isCtxNull ? null : verticleVertxCtx

        when: 'check if verticle is deployed'
        def actualRes = verticle.isDeployed()

        then:
        noExceptionThrown()
        actualRes == expectedIsDeployed

        and:
        (0..1) * verticleVertxCtx.deploymentID() >> depId

        where:
        isCtxNull | depId       || expectedIsDeployed
        true      | 'some-uuid' || false
        true      | null        || false
        false     | null        || false
        false     | 'some-uuid' || true
    }
}
