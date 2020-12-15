package com.dburyak.vertx.core

import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanRegistration
import io.micronaut.inject.BeanIdentifier
import io.reactivex.Completable
import spock.lang.Timeout

import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 2, unit = SECONDS)
class VerticleSpec extends BaseVertxRxJavaSpec {
    Verticle verticle = Spy(Verticle)

    ApplicationContext verticleBeanCtx = Mock(ApplicationContext)
    BeanRegistration beanCtxBeanReg = Mock(BeanRegistration)
    BeanRegistration verticleBeanReg = Mock(BeanRegistration)
    BeanIdentifier verticleBeanId = Mock(BeanIdentifier)
    BeanIdentifier beanCtxBeanId = Mock(BeanIdentifier)

    void setup() {
        verticle.@verticleBeanCtx = verticleBeanCtx
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
}
