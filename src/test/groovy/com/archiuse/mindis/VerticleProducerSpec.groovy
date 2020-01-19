package com.archiuse.mindis

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class VerticleProducerSpec extends Specification {
    VerticleProducer verticleProducer = Spy(VerticleProducer)
    MindisVerticle producedVerticle = Mock(MindisVerticle)

    def 'supplier calls doCreateVerticle factory method'() {
        when:
        verticleProducer.verticleSupplier.get()

        then:
        noExceptionThrown()
        1 * verticleProducer.doCreateVerticle() >> producedVerticle
    }

    def 'supplier initializes bean context of the produced verticle'() {
        given:
        def beanCtx = Mock(ApplicationContext)
        verticleProducer.verticleBeanCtx = beanCtx

        when:
        def res = verticleProducer.verticleSupplier.get()

        then:
        noExceptionThrown()
        res == producedVerticle

        and:
        1 * verticleProducer.doCreateVerticle() >> producedVerticle
        1 * producedVerticle.setProperty('verticleBeanCtx', beanCtx)
    }
}
