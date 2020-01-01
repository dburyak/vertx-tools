package com.archiuse.mindis


import spock.lang.Specification
import spock.lang.Timeout

import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 2, unit = SECONDS)
class MindisVerticleSpec extends Specification {

    MindisVerticle mindisVerticle = Spy(MindisVerticle)

    def 'rxStart calls doStart'() {
        when: 'call rxStart'
        mindisVerticle.rxStart().test().await()

        then: 'doStart is called'
        noExceptionThrown()
        1 * mindisVerticle.doStart()

    }

    def 'rxStop callsDoStop'() {
        when: 'call rxStop'
        mindisVerticle.rxStop().test().await()

        then: 'doStop is called'
        noExceptionThrown()
        1 * mindisVerticle.doStop()
    }
}
