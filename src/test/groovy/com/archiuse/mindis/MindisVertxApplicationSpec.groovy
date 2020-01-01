package com.archiuse.mindis


import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.BeanContext
import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.reactivex.core.Vertx
import spock.lang.Specification
import spock.lang.Timeout

import static com.archiuse.mindis.MindisVertxApplication.PROP_IS_APP_BEAN_CTX
import static java.util.concurrent.TimeUnit.SECONDS
import static java.util.regex.Matcher.quoteReplacement

@Timeout(value = 2, unit = SECONDS)
class MindisVertxApplicationSpec extends Specification {

    MindisVertxApplication mindisVertxApplication = Spy(MindisVertxApplication)

    def depIds = ['depId1', 'depId2']
    def verticleClasses = [TestMindisVerticle1, TestMindisVerticle2]
    def verticleNames = [
            TestMindisVerticle1.canonicalName.replaceFirst(
                    "\\.${TestMindisVerticle1.simpleName}", quoteReplacement("\$${TestMindisVerticle1.simpleName}")),
            TestMindisVerticle2.canonicalName.replaceFirst(
                    "\\.${TestMindisVerticle2.simpleName}", quoteReplacement("\$${TestMindisVerticle2.simpleName}")),
    ]

    // mocks
    def appCtxBuilder = Mock(ApplicationContextBuilder)
    def appCtx = Mock(ApplicationContext)
    def vertx = Mock(Vertx)
    def verticle1 = Mock(TestMindisVerticle1)
    def verticle2 = Mock(TestMindisVerticle2)
    def beanCtx1 = Mock(BeanContext)
    def beanCtx2 = Mock(BeanContext)

    // static methods mock support
    volatile int invocationsBuildAppCtx = 0
    volatile int invocationsBuildBeanCtx = 0

    void setup() {
        ApplicationContext.metaClass.static.build = {
            invocationsBuildAppCtx++
            appCtxBuilder
        }
        BeanContext.metaClass.static.run = {
            def result = (invocationsBuildBeanCtx == 0) ? beanCtx1 : beanCtx2
            invocationsBuildBeanCtx++
            result
        }
    }

    def 'start performs necessary actions'() {
        when:
        def startResult = mindisVertxApplication.start().test().await()

        then:
        noExceptionThrown()

        and: 'application context is built correctly'
        invocationsBuildAppCtx == 1
        1 * appCtxBuilder.properties([(PROP_IS_APP_BEAN_CTX): true]) >> appCtxBuilder
        1 * appCtxBuilder.start() >> appCtx

        and: 'new application context is stored'
        mindisVertxApplication.applicationContext == appCtx

        and: 'bean contexts are created for each verticle'
        1 * appCtx.getBean(Vertx) >> vertx
        1 * mindisVertxApplication.getVerticleNames() >> verticleNames
        invocationsBuildBeanCtx == 2

        and: 'app beans are injected into each bean context'
        2 * appCtx.getBean(Vertx) >> vertx
        1 * beanCtx1.registerSingleton(ApplicationContext, appCtx)
        1 * beanCtx1.registerSingleton(Vertx, vertx)
        1 * beanCtx2.registerSingleton(ApplicationContext, appCtx)
        1 * beanCtx2.registerSingleton(Vertx, vertx)

        and: 'verticles are deployed'
        1 * beanCtx1.getBean(verticleClasses[0]) >> verticle1
        1 * vertx.rxDeployVerticle(verticle1) >> Single.just(depIds[0])
        1 * beanCtx2.getBean(verticleClasses[1]) >> verticle2
        1 * vertx.rxDeployVerticle(verticle2) >> Single.just(depIds[1])

        and: 'bean contexts and deployment ids are stored'
        mindisVertxApplication.beanContexts[depIds[0]].v1 == verticleNames[0]
        mindisVertxApplication.beanContexts[depIds[0]].v2 == beanCtx1
        mindisVertxApplication.beanContexts[depIds[1]].v1 == verticleNames[1]
        mindisVertxApplication.beanContexts[depIds[1]].v2 == beanCtx2

        and:
        startResult.assertComplete()
    }

    def 'stop performs necessary actions'() {
        given:
        mindisVertxApplication.applicationContext = appCtx
        mindisVertxApplication.beanContexts[depIds[0]] = new Tuple2<>(verticleNames[0], beanCtx1)
        mindisVertxApplication.beanContexts[depIds[1]] = new Tuple2<>(verticleNames[1], beanCtx2)

        when:
        def stopResult = mindisVertxApplication.stop().test().await()

        then:
        noExceptionThrown()

        and: 'vertx is closed'
        1 * appCtx.getBean(Vertx) >> vertx
        1 * vertx.rxClose() >> Completable.complete()

        and: 'each bean context is closed'
        1 * beanCtx1.stop() >> beanCtx1
        1 * beanCtx2.stop() >> beanCtx2

        and: 'bean contexts are removed from registry map'
        !(depIds[0] in mindisVertxApplication.beanContexts.keys())
        !(depIds[1] in mindisVertxApplication.beanContexts.keys())

        and: 'application context is stopped'
        1 * appCtx.stop() >> appCtx

        and: 'application context is not stored anymore'
        mindisVertxApplication.applicationContext == null

        and:
        stopResult.assertComplete()
    }

    static class TestMindisVerticle1 extends MindisVerticle {
    }

    static class TestMindisVerticle2 extends MindisVerticle {
    }
}
