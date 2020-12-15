package com.dburyak.vertx.core


import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class VerticleProducerSpec extends Specification {
    VerticleProducer verticleProducer = Spy(VerticleProducer)
    Verticle producedVerticle = Mock(Verticle)
    ApplicationContext verticleBeanCtx = Mock(ApplicationContext)

    def 'verticle producer calls doCreateVerticle factory method on initialization'() {
        given: 'correctly initialized verticle producer'
        verticleProducer.verticleBeanCtx = verticleBeanCtx

        when: 'produce new verticle'
        verticleProducer.get()

        then:
        noExceptionThrown()
        1 * verticleProducer.doCreateVerticle() >> producedVerticle
    }

    def 'verticle producer initializes bean context of the produced verticle'() {
        given: 'correctly initialized verticle producer'
        verticleProducer.verticleBeanCtx = verticleBeanCtx

        when: 'produce new verticle'
        def res = verticleProducer.get()

        then: 'verticle produced'
        noExceptionThrown()
        res == producedVerticle

        and: 'verticle bean context of produced verticle is populated with value from producer'
        producedVerticle.@verticleBeanCtx == verticleBeanCtx

        and:
        1 * verticleProducer.doCreateVerticle() >> producedVerticle
    }

    def 'verticle producer fails if verticle bean context is not specified'() {
        given: 'verticle producer with no bean context initialized'

        when: 'produce new verticle'
        def res = verticleProducer.get()

        then: 'production fails'
        thrown IllegalStateException
    }
}
