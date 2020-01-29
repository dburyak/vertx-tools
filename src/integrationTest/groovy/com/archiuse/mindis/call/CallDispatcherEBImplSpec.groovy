package com.archiuse.mindis.call

import com.archiuse.mindis.test.integration.Async
import com.archiuse.mindis.test.integration.AsyncCompletion
import com.archiuse.mindis.test.integration.VertxIntegrationSpec
import groovy.util.logging.Slf4j
import spock.lang.Timeout

import javax.inject.Inject
import java.time.Duration

import static java.util.concurrent.TimeUnit.SECONDS

/**
 * NOTE: we use small delays in this suite because lots of "a -> b" effects in related functionality depend on service
 * discovery. And at the moment we don't have any relationship between "publish something to service discovery" and
 * "it is picked up by some particular service" events. And probably we wouldn't have those as they are not needed
 * for all foreseen use cases.
 */
@Slf4j
@Timeout(value = 3, unit = SECONDS)
class CallDispatcherEBImplSpec extends VertxIntegrationSpec {
    private static Duration SERVICE_DISCOVERY_DELAY = Duration.ofMillis 10

    @Inject
    CallDispatcherEBImpl callDispatcher

    @Inject
    CallReceiverEBImpl callReceiver

    @AsyncCompletion(numActions = 2)
    def 'call the same verticle service works correctly'(Async async) {
        given: 'CALL service registered on this test verticle'
        def rcv = integrationTestVerticle.receiverName
        def action = 'action'
        def sentValue = 'value'
        callReceiver
                .onCall(rcv, action) { receivedValue, headers ->
                    async.doAssertAndMarkStepDone {
                        // then: call handler received correct value
                        assert receivedValue == sentValue
                        assert !headers
                    }
                }
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { reg ->
                    // when: do CALL service from this verticle and send value
                    callDispatcher.call(rcv, action, sentValue)
                            .delay(SERVICE_DISCOVERY_DELAY)
                            .doOnTerminate { reg.dispose() }
                }
                .subscribe({
                    async.stepDone()
                }, {
                    async.fail it
                })
    }
}
