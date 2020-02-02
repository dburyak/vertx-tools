package com.archiuse.mindis.call

import com.archiuse.mindis.VerticleProducer
import com.archiuse.mindis.test.integration.Async
import com.archiuse.mindis.test.integration.AsyncCompletion
import com.archiuse.mindis.test.integration.StackVerticle
import com.archiuse.mindis.test.integration.VertxIntegrationSpec
import groovy.util.logging.Slf4j
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import spock.lang.Timeout

import javax.inject.Inject
import java.time.Duration

import static com.archiuse.mindis.test.integration.StackVerticle.ACTION_POP
import static com.archiuse.mindis.test.integration.StackVerticle.ACTION_PUSH
import static com.archiuse.mindis.test.integration.StackVerticle.ACTION_SIZE
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
    private static Duration SERVICE_DISCOVERY_DELAY = Duration.ofMillis 20
    private static Duration CALL_ASYNC_COMPLETION_DELAY = Duration.ofMillis 20

    @Inject
    CallDispatcherEBImpl callDispatcher

    @Inject
    CallReceiverEBImpl callReceiver

    @AsyncCompletion(numActions = 2)
    def 'call with no headers to the same verticle service passes data correctly'(Async async) {
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

    @AsyncCompletion(numActions = 2)
    def 'call with headers to the same verticle service passes data and headers correctly'(Async async) {
        given: 'CALL service registered on this test verticle'
        def rcv = integrationTestVerticle.receiverName
        def action = 'action'
        def sentValue = 'value'
        def sentHeaders = [one: '1', two: ['hello', 'world'], three: Duration.ofSeconds(7) as String]
        callReceiver
                .onCall(rcv, action) { receivedValue, receivedHeaders ->
                    async.doAssertAndMarkStepDone {
                        // then: call handler received correct value
                        assert receivedValue == sentValue
                        assert receivedHeaders == sentHeaders
                    }
                }
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { reg ->
                    // when: do CALL service from this verticle and send value and headers
                    callDispatcher.call(rcv, action, sentValue, sentHeaders)
                            .delay(SERVICE_DISCOVERY_DELAY)
                            .doOnTerminate { reg.dispose() }
                }
                .subscribe({
                    async.stepDone()
                }, {
                    async.fail it
                })
    }

    @AsyncCompletion(numActions = 6)
    def 'call with no headers to the other verticle service passes data correctly'(Async async) {
        given: 'stack verticle deployed'
        def stack = 'StackVerticle'
        def stackProducer = { new StackVerticle(receiverName: stack) } as VerticleProducer
        stackProducer.name = 'StackVerticleProducer'
        def value = 'value'
        app
                .deployVerticle(stackProducer)
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { stackDeploymentId ->
                    def doTests = callDispatcher
                            .request(stack, ACTION_SIZE)
                            .flatMap { initialStackSize ->
                                async.stepDone()

                                // when: 'CALL push value to stack'
                                callDispatcher
                                        .call(stack, ACTION_PUSH, value)
                                        .delay(CALL_ASYNC_COMPLETION_DELAY)
                                        .doOnComplete { async.stepDone() }

                                // then: 'stack size was incremented'
                                        .andThen(callDispatcher.request(stack, ACTION_SIZE))
                                        .doOnSuccess {
                                            assert it == initialStackSize + 1
                                            async.stepDone()
                                        }

                                // when: 'pop value via REQUEST from stack verticle'
                                        .flatMap { callDispatcher.request(stack, ACTION_POP) }

                                // then: 'popped value is correct'
                                        .doOnSuccess {
                                            assert it == value
                                            async.stepDone()
                                        }

                                // and: 'stack size was decremented'
                                        .flatMap { callDispatcher.request(stack, ACTION_SIZE) }
                                        .doOnSuccess {
                                            assert it == initialStackSize
                                            async.stepDone()
                                        }
                            }

                    def undeployStack = app.undeployVerticle(stackDeploymentId).toMaybe()

                    Maybe.concatDelayError([doTests, undeployStack]).ignoreElements()
                }
                .subscribe({
                    async.stepDone()
                }, {
                    async.fail it
                })
    }

    @AsyncCompletion(numActions = 6)
    def 'call with headers to the other verticle passes data correctly'(Async async) {
        given: 'stack verticle deployed'
        def stack = 'StackVerticle'
        def stackProducer = { new StackVerticle(receiverName: stack) } as VerticleProducer
        stackProducer.name = 'StackVerticleProducer'
        def value = 'value'
        def suffix = '_suffix'
        app
                .deployVerticle(stackProducer)
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { stackDeploymentId ->
                    def doTests = callDispatcher
                            .request(stack, ACTION_SIZE)
                            .flatMap { initialStackSize ->
                                async.stepDone()

                                // when: 'CALL push value to stack'
                                callDispatcher
                                        .call(stack, ACTION_PUSH, value, [suffix: suffix])
                                        .delay(CALL_ASYNC_COMPLETION_DELAY)
                                        .doOnComplete { async.stepDone() }

                                // then: 'stack size was incremented'
                                        .andThen(callDispatcher.request(stack, ACTION_SIZE))
                                        .doOnSuccess {
                                            assert it == initialStackSize + 1
                                            async.stepDone()
                                        }

                                // when: 'pop value via REQUEST from stack verticle'
                                        .flatMap { callDispatcher.request(stack, ACTION_POP) }

                                // then: 'popped value is correct'
                                        .doOnSuccess {
                                            assert it == value + suffix
                                            async.stepDone()
                                        }

                                // and: 'stack size was decremented'
                                        .flatMap { callDispatcher.request(stack, ACTION_SIZE) }
                                        .doOnSuccess {
                                            assert it == initialStackSize
                                            async.stepDone()
                                        }
                            }

                    def undeployStack = app.undeployVerticle(stackDeploymentId).toMaybe()

                    Maybe.concatDelayError([doTests, undeployStack]).ignoreElements()
                }
                .subscribe({
                    async.stepDone()
                }, {
                    async.fail it
                })
    }

    @AsyncCompletion(numActions = 2)
    def 'publish to the same verticle service topic publishes data correctly'(Async async) {
        given: 'PUBLISH service registered on this verticle'
        def rcv = integrationTestVerticle.receiverName
        def action = 'action'
        def sentValue = 'value'
        callReceiver
                .subscribe(rcv, action) { receivedValue, headers ->
                    async.doAssertAndMarkStepDone {
                        // then: subscription handler received correct value
                        assert receivedValue == sentValue
                        assert !headers
                    }
                }
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { reg ->
                    // when: do PUBLISH value from this verticle
                    callDispatcher.publish(rcv, action, sentValue)
                            .delay(SERVICE_DISCOVERY_DELAY)
                            .doOnTerminate { reg.dispose() }
                }
                .subscribe({
                    async.stepDone()
                }, {
                    async.fail it
                })
    }

    @AsyncCompletion(numActions = 4)
    def 'publish to the same verticle service topic publishes data correctly for three subscribers'(Async async) {
        given: 'PUBLISH service registered on this verticle'
        def rcv = integrationTestVerticle.receiverName
        def action = 'action'
        def sentValue = 'value'

        Observable
                .range(0, 3)
                .flatMapSingle {
                    callReceiver.subscribe(rcv, action) { receivedValue, headers ->
                        async.doAssertAndMarkStepDone {
                            // then: subscription handler received correct value
                            assert receivedValue == sentValue
                            assert !headers
                        }
                    }
                }
                .toList()
                .map { new CompositeDisposable(it) }
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { reg ->
                    // when: do PUBLISH value from this verticle
                    callDispatcher.publish(rcv, action, sentValue)
                            .delay(SERVICE_DISCOVERY_DELAY)
                            .doOnTerminate { reg.dispose() }
                }
                .subscribe({
                    async.stepDone()
                }, {
                    async.fail it
                })
    }

    @AsyncCompletion(numActions = 2)
    def 'publish with headers to the same verticle service topic publishes data correctly'(Async async) {
        given: 'PUBLISH service registered on this verticle'
        def rcv = integrationTestVerticle.receiverName
        def action = 'action'
        def sentValue = 'value'
        def sentHeaders = [one: '1', two: ['hello', 'world'], three: Duration.ofSeconds(7) as String]
        callReceiver
                .subscribe(rcv, action) { receivedValue, receivedHeaders ->
                    async.doAssertAndMarkStepDone {
                        // then: subscription handler received correct value
                        assert receivedValue == sentValue
                        assert receivedHeaders == sentHeaders
                    }
                }
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { reg ->
                    // when: do PUBLISH value from this verticle
                    callDispatcher.publish(rcv, action, sentValue, sentHeaders)
                            .delay(SERVICE_DISCOVERY_DELAY)
                            .doOnTerminate { reg.dispose() }
                }
                .subscribe({
                    async.stepDone()
                }, {
                    async.fail it
                })
    }

    @AsyncCompletion(numActions = 4)
    def 'publish with headers to the same verticle service topic publishes data correctly for three subscribers'(
            Async async) {
        given: 'PUBLISH service registered on this verticle'
        def rcv = integrationTestVerticle.receiverName
        def action = 'action'
        def sentValue = 'value'
        def sentHeaders = [one: '1', two: ['hello', 'world'], three: Duration.ofSeconds(7) as String]
        Observable
                .range(0, 3)
                .flatMapSingle {
                    callReceiver.subscribe(rcv, action) { receivedValue, receivedHeaders ->
                        async.doAssertAndMarkStepDone {
                            // then: subscription handler received correct value
                            assert receivedValue == sentValue
                            assert receivedHeaders == sentHeaders
                        }
                    }
                }
                .toList()
                .map { new CompositeDisposable(it) }
                .delay(SERVICE_DISCOVERY_DELAY)
                .flatMapCompletable { reg ->
                    // when: do PUBLISH value from this verticle
                    callDispatcher.publish(rcv, action, sentValue, sentHeaders)
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
