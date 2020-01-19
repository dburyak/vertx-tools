package com.archiuse.mindis

import com.archiuse.mindis.call.ServiceHelper
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanRegistration
import io.micronaut.inject.BeanIdentifier
import io.reactivex.Completable
import spock.lang.Timeout

import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 2, unit = SECONDS)
class MindisVerticleSpec extends VertxRxJavaSpec {

    MindisVerticle mindisVerticle = Spy(MindisVerticle)

    ApplicationContext verticleBeanCtx = Mock(ApplicationContext)
    ServiceHelper serviceHelper = Mock(ServiceHelper)
    BeanRegistration beanCtxBeanReg = Mock(BeanRegistration)
    BeanRegistration verticleBeanReg = Mock(BeanRegistration)
    BeanIdentifier verticleBeanId = Mock(BeanIdentifier)
    BeanIdentifier beanCtxBeanId = Mock(BeanIdentifier)

    void setup() {
        mindisVerticle.@verticleBeanCtx = verticleBeanCtx
    }

    def 'rxStart calls doStart'() {
        when: 'call rxStart'
        mindisVerticle.rxStart().test().await()

        then: 'doStart is called'
        noExceptionThrown()
        1 * mindisVerticle.doStart()

    }

    def 'rxStart inits bean context'() {
        when: 'call rxStart'
        def res = mindisVerticle.rxStart().test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()

        and:
        1 * verticleBeanCtx.registerSingleton(ApplicationContext, verticleBeanCtx, _) >> verticleBeanCtx
        1 * verticleBeanCtx.registerSingleton(mindisVerticle)

        1 * verticleBeanCtx.findBeanRegistration(verticleBeanCtx) >> Optional.of(beanCtxBeanReg)
        1 * beanCtxBeanReg.identifier >> beanCtxBeanId
        1 * verticleBeanCtx.refreshBean(beanCtxBeanId)

        1 * verticleBeanCtx.findBeanRegistration(mindisVerticle) >> Optional.of(verticleBeanReg)
        1 * verticleBeanReg.identifier >> verticleBeanId
        1 * verticleBeanCtx.refreshBean(verticleBeanId)

        0 * mindisVerticle.doStop()
    }

    def 'rxStop callsDoStop'() {
        when: 'call rxStop'
        mindisVerticle.rxStop().test().await()

        then: 'doStop is called'
        noExceptionThrown()
        1 * mindisVerticle.doStop()
    }

    def 'init sets up default values for call receiver description'() {
        given:
        def healthActionName = 'test-health'
        def readinessActionName = 'test-readiness'

        when:
        mindisVerticle.init(serviceHelper)

        then:
        noExceptionThrown()
        mindisVerticle.receiverName == mindisVerticle.getClass().canonicalName
        1 * serviceHelper.getProperty('healthAction') >> healthActionName
        1 * serviceHelper.getProperty('readinessAction') >> readinessActionName
        healthActionName in mindisVerticle.actions
        readinessActionName in mindisVerticle.actions
    }

    def 'doStart failure results in async exception of all the start chain'() {
        when:
        def res = mindisVerticle.rxStart().test().await()

        then:
        noExceptionThrown()
        res.assertError(Exception)

        and:
        1 * mindisVerticle.doStart() >> Completable.error(new Exception())

        and:
        1 * verticleBeanCtx.registerSingleton(ApplicationContext, verticleBeanCtx, _) >> verticleBeanCtx
        1 * verticleBeanCtx.registerSingleton(mindisVerticle)

        1 * verticleBeanCtx.findBeanRegistration(verticleBeanCtx) >> Optional.of(beanCtxBeanReg)
        1 * beanCtxBeanReg.identifier >> beanCtxBeanId
        1 * verticleBeanCtx.refreshBean(beanCtxBeanId)

        1 * verticleBeanCtx.findBeanRegistration(mindisVerticle) >> Optional.of(verticleBeanReg)
        1 * verticleBeanReg.identifier >> verticleBeanId
        1 * verticleBeanCtx.refreshBean(verticleBeanId)
    }

    def 'doStop failure results in async exception of all the stop chain'() {
        when:
        def res = mindisVerticle.rxStop().test().await()

        then:
        noExceptionThrown()
        res.assertError(Exception)

        and:
        1 * mindisVerticle.doStop() >> Completable.error(new Exception())
    }
}
