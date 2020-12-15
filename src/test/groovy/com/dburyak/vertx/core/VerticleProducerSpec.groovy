package com.dburyak.vertx.core


import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class VerticleProducerSpec extends Specification {
    MicronautVerticleProducer verticleProducer = Spy(MicronautVerticleProducer)
    Verticle producedVerticle = Mock(Verticle)
    ApplicationContext verticleBeanCtx = Mock(ApplicationContext)

    def 'verticle producer calls doCreateVerticle factory method on initialization'() {
        given:
        verticleProducer.verticleBeanCtx = verticleBeanCtx

        when:
        verticleProducer.get()

        then:
        noExceptionThrown()
        1 * verticleProducer.doCreateVerticle() >> producedVerticle
    }

    def 'verticle producer initializes bean context of the produced verticle'() {
        given:
        verticleProducer.verticleBeanCtx = verticleBeanCtx

        when:
        def res = verticleProducer.get()

        then:
        noExceptionThrown()
        res == producedVerticle
        producedVerticle.@verticleBeanCtx == verticleBeanCtx

        and:
        1 * verticleProducer.doCreateVerticle() >> producedVerticle
    }
}
