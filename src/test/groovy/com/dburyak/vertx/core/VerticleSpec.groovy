package com.dburyak.vertx.core

import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanRegistration
import io.micronaut.inject.BeanIdentifier
import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.core.Context
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.file.FileSystem

import java.time.Instant
import java.time.ZoneId
//@Timeout(value = 2, unit = SECONDS)
class VerticleSpec extends BaseVertxRxJavaSpec {
    Verticle verticle = Spy(Verticle)

    FileSystem fs = Mock(FileSystem)
    Context verticleVertxCtx = Mock(Context)
    ApplicationContext verticleBeanCtx = Mock(ApplicationContext)
    BeanRegistration beanCtxBeanReg = Mock(BeanRegistration)
    BeanRegistration verticleBeanReg = Mock(BeanRegistration)
    BeanIdentifier verticleBeanId = Mock(BeanIdentifier)
    BeanIdentifier beanCtxBeanId = Mock(BeanIdentifier)

    void setup() {
        verticle.@verticleBeanCtx = verticleBeanCtx
        verticle.@context = verticleVertxCtx
        verticle.fs = fs
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
        res.assertValue {it.timezone == ZoneId.systemDefault().id }
        res.assertValue { it.verticle.deployment_id == 'deploymentId-uuid' }
        res.assertValue { it.verticle.type == 'EventLoop' }
        res.assertValue { it.verticle.thread.name != null }
        res.assertValue { it.verticle.thread.id != null }
        res.assertValue { it.verticle.thread.vertx_thread != null }
        res.assertValue { it.verticle.thread.event_loop_thread != null }
        res.assertValue { it.verticle.thread.worker_thread != null }
        res.assertValue { it.verticle.containsKey('config') }

        and:
        2 * verticleVertxCtx.deploymentID() >> 'deploymentId-uuid'
        1 * fs.rxReadFile('version.txt') >> Single.just(Buffer.buffer('1.0'))
        1 * fs.rxReadFile('revision.txt') >> Single.just(Buffer.buffer('abcdef0123456789'))
        1 * fs.rxReadFile('built_at.txt') >> Single.just(Buffer.buffer('2020-12-18T22:28:05.165893419Z'))
        1 * verticleVertxCtx.isEventLoopContext() >> true
    }
}
