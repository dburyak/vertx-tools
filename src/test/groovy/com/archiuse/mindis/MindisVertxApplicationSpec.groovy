package com.archiuse.mindis

import com.archiuse.mindis.di.AppBean
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.inject.qualifiers.Qualifiers
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
    def verticleBeanCtxBuilder1 = Mock(ApplicationContextBuilder)
    def verticleBeanCtx1 = Mock(ApplicationContext)
    def verticleBeanCtxBuilder2 = Mock(ApplicationContextBuilder)
    def verticleBeanCtx2 = Mock(ApplicationContext)

    // static methods mock support
    volatile int invocationsBuildBeanCtx = 0

    void setup() {
        ApplicationContext.metaClass.static.build = {
            def result = (invocationsBuildBeanCtx == 0) ? appCtxBuilder
                    : (invocationsBuildBeanCtx == 1) ? verticleBeanCtxBuilder1
                    : (invocationsBuildBeanCtx == 2) ? verticleBeanCtxBuilder2
                    : null
            assert result: 'more invocations than expected'
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
        invocationsBuildBeanCtx == 3
        1 * appCtxBuilder.properties([(PROP_IS_APP_BEAN_CTX): true]) >> appCtxBuilder
        1 * appCtxBuilder.start() >> appCtx

        and: 'new application context is stored'
        mindisVertxApplication.applicationBeanContext == appCtx

        and: 'bean contexts are created for each verticle'
        1 * verticleBeanCtxBuilder1.properties([(PROP_IS_APP_BEAN_CTX): false]) >> verticleBeanCtxBuilder1
        1 * verticleBeanCtxBuilder1.start() >> verticleBeanCtx1
        1 * verticleBeanCtxBuilder2.properties([(PROP_IS_APP_BEAN_CTX): false]) >> verticleBeanCtxBuilder2
        1 * verticleBeanCtxBuilder2.start() >> verticleBeanCtx2
        1 * appCtx.getBean(Vertx) >> vertx
        1 * mindisVertxApplication.getVerticlesProducers() >> verticleNames

        and: 'app beans are injected into each bean context'
        2 * appCtx.getBean(Vertx) >> vertx
        1 * verticleBeanCtx1.registerSingleton(ApplicationContext, appCtx, Qualifiers.byStereotype(AppBean))
        1 * verticleBeanCtx1.registerSingleton(Vertx, vertx)
        1 * verticleBeanCtx2.registerSingleton(ApplicationContext, appCtx, Qualifiers.byStereotype(AppBean))
        1 * verticleBeanCtx2.registerSingleton(Vertx, vertx)

        and: 'verticles are deployed'
        1 * verticleBeanCtx1.getBean(verticleClasses[0]) >> verticle1
        1 * vertx.rxDeployVerticle(verticle1) >> Single.just(depIds[0])
        1 * verticleBeanCtx2.getBean(verticleClasses[1]) >> verticle2
        1 * vertx.rxDeployVerticle(verticle2) >> Single.just(depIds[1])

        and: 'bean contexts and deployment ids are stored'
        mindisVertxApplication.verticlesBeanContexts[depIds[0]].v1 == verticleNames[0]
        mindisVertxApplication.verticlesBeanContexts[depIds[0]].v2 == verticleBeanCtx1
        mindisVertxApplication.verticlesBeanContexts[depIds[1]].v1 == verticleNames[1]
        mindisVertxApplication.verticlesBeanContexts[depIds[1]].v2 == verticleBeanCtx2

        and:
        startResult.assertComplete()
    }

    def 'stop performs necessary actions'() {
        given:
        mindisVertxApplication.applicationBeanContext = appCtx
        mindisVertxApplication.verticlesBeanContexts[depIds[0]] = new Tuple2<>(verticleNames[0], verticleBeanCtx1)
        mindisVertxApplication.verticlesBeanContexts[depIds[1]] = new Tuple2<>(verticleNames[1], verticleBeanCtx2)

        when:
        def stopResult = mindisVertxApplication.stop().test().await()

        then:
        noExceptionThrown()

        and: 'vertx is closed'
        1 * appCtx.getBean(Vertx) >> vertx
        1 * vertx.rxClose() >> Completable.complete()

        and: 'each bean context is closed'
        1 * verticleBeanCtx1.stop() >> verticleBeanCtx1
        1 * verticleBeanCtx2.stop() >> verticleBeanCtx2

        and: 'bean contexts are removed from registry map'
        !(depIds[0] in mindisVertxApplication.verticlesBeanContexts.keys())
        !(depIds[1] in mindisVertxApplication.verticlesBeanContexts.keys())

        and: 'application context is stopped'
        1 * appCtx.stop() >> appCtx

        and: 'application context is not stored anymore'
        mindisVertxApplication.applicationBeanContext == null

        and:
        stopResult.assertComplete()
    }

    static class TestMindisVerticle1 extends MindisVerticle {
    }

    static class TestMindisVerticle2 extends MindisVerticle {
    }
}
