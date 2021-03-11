package com.archiuse.mindis.call

import com.archiuse.mindis.VertxRxJavaSpec
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.reactivex.core.eventbus.EventBus
import io.vertx.reactivex.core.eventbus.Message
import io.vertx.reactivex.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.Record
import spock.lang.Timeout

import java.util.function.Function

import static com.archiuse.mindis.call.ServiceType.CALL
import static com.archiuse.mindis.call.ServiceType.PUB_SUB_TOPIC
import static com.archiuse.mindis.call.ServiceType.REQUEST_RESPONSE
import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 2, unit = SECONDS)
class CallDispatcherOldEBImplSpec extends VertxRxJavaSpec {

    CallDispatcherEBImpl callDispatcher = new CallDispatcherEBImpl()

    EBServiceImplHelper ebServiceImplHelper = Mock(EBServiceImplHelper)
    LocalEBAwareJsonMessageCodec ebMsgCodec = Mock(LocalEBAwareJsonMessageCodec)
    EventBus eventBus = Mock(EventBus)
    Message respMsg = Mock(Message)
    ServiceDiscovery serviceDiscovery = Mock(ServiceDiscovery)

    def rcv = 'receiver'
    def action = 'action'
    def callAddr = 'call:receiver:action'
    def reqRespAddr = 'request_response:receiver:action'
    def topicAddr = 'topic:receiver:action'
    def args = ['arg1', 'arg2', 'arg3']
    def codec = 'codec'
    def opts = new DeliveryOptions()
    def srvName = 'receiver/action'
    def respBody = 'response data'

    void setup() {
        callDispatcher.ebServiceImplHelper = ebServiceImplHelper
        callDispatcher.ebMsgCodec = ebMsgCodec
        callDispatcher.eventBus = eventBus
        callDispatcher.serviceDiscovery = serviceDiscovery
    }

    def 'call performs correct actions'() {
        setup:
        callDispatcher.ebAddr[srvName] = callAddr
        callDispatcher.serviceTypes[srvName] = CALL

        when:
        def res = callDispatcher.call(rcv, action, args, opts).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()
        res.assertNoValues()

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        1 * ebMsgCodec.name() >> codec
        1 * eventBus.send(callAddr, args, { it instanceof DeliveryOptions && it.codecName == codec })
    }

    def 'call wit no delivery options provided sets up codec correctly'() {
        setup:
        callDispatcher.ebAddr[srvName] = callAddr
        callDispatcher.serviceTypes[srvName] = CALL

        when:
        def res = callDispatcher.call(rcv, action, args).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()
        res.assertNoValues()

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        1 * ebMsgCodec.name() >> codec
        1 * eventBus.send(callAddr, args, { it instanceof DeliveryOptions && it.codecName == codec })
    }

    def 'call throws when ebAddr for service is not registered'() {
        given: 'service is not registered'
        callDispatcher.ebAddr.remove(srvName)

        when: 'call unregistered service'
        def res = callDispatcher.call(rcv, action, args, opts).test().await()

        then: 'ServiceNotFoundException is thrown correctly'
        noExceptionThrown()
        res.assertTerminated()
        res.assertError { err ->
            err instanceof ServiceNotFoundException
                    && err.receiver == rcv
                    && err.action == action
        }

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
    }

    def 'call throws when service has non-matching type'() {
        given: 'service is registered with REQUEST_RESPONSE type'
        callDispatcher.ebAddr[srvName] = reqRespAddr
        callDispatcher.serviceTypes[srvName] = REQUEST_RESPONSE

        when: 'do CALL service'
        def res = callDispatcher.call(rcv, action, args, opts).test().await()

        then: 'WrongServiceTypeException is thrown correctly'
        noExceptionThrown()
        res.assertTerminated()
        res.assertError { err ->
            err instanceof WrongServiceTypeException
                    && err.receiver == rcv
                    && err.action == action
                    && err.expectedType == CALL
                    && err.actualType == REQUEST_RESPONSE
        }

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
    }

    def 'call with no args delegates correctly with args=null'() {
        setup:
        callDispatcher = Spy(callDispatcher)

        when:
        def res = callDispatcher.call(rcv, action, opts).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()
        res.assertNoValues()

        and:
        1 * callDispatcher.call(rcv, action, null, opts) >> Completable.complete()
    }

    def 'request with empty response performs correct actions'() {
        given:
        callDispatcher.ebAddr[srvName] = reqRespAddr
        callDispatcher.serviceTypes[srvName] = REQUEST_RESPONSE

        when:
        def res = callDispatcher.request(rcv, action, args, opts).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertNoValues()
        res.assertComplete()

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        1 * ebMsgCodec.name() >> codec
        1 * eventBus.rxRequest(reqRespAddr, args, { it instanceof DeliveryOptions && it.codecName == codec }) >>
                Single.just(respMsg)
        1 * respMsg.body() >> null
    }

    def 'request with non-empty response performs correct actions'() {
        given:
        callDispatcher.ebAddr[srvName] = reqRespAddr
        callDispatcher.serviceTypes[srvName] = REQUEST_RESPONSE

        when:
        def res = callDispatcher.request(rcv, action, args, opts).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()
        res.assertValue(respBody)

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        1 * ebMsgCodec.name() >> codec
        1 * eventBus.rxRequest(reqRespAddr, args, { it instanceof DeliveryOptions && it.codecName == codec }) >>
                Single.just(respMsg)
        1 * respMsg.body() >> respBody
    }

    def 'request with no delivery options provided sets up codec correctly'() {
        given:
        callDispatcher.ebAddr[srvName] = reqRespAddr
        callDispatcher.serviceTypes[srvName] = REQUEST_RESPONSE

        when:
        def res = callDispatcher.request(rcv, action, args).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()
        res.assertValue(respBody)

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        1 * ebMsgCodec.name() >> codec
        1 * eventBus.rxRequest(reqRespAddr, args, { it instanceof DeliveryOptions && it.codecName == codec }) >>
                Single.just(respMsg)
        1 * respMsg.body() >> respBody
    }

    def 'request with no args delegates correctly with args=null'() {
        setup:
        callDispatcher = Spy(callDispatcher)

        when:
        def res = callDispatcher.request(rcv, action, opts).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()

        and:
        1 * callDispatcher.request(rcv, action, null, opts) >> Maybe.just(respBody)
    }

    def 'request throws when ebAddr for service is not registered'() {
        given: 'service is not registered'
        callDispatcher.ebAddr.remove(srvName)

        when: 'request unregistered service'
        def res = callDispatcher.request(rcv, action, args, opts).test().await()

        then: 'ServiceNotFoundException is thrown correctly'
        noExceptionThrown()
        res.assertTerminated()
        res.assertError { err ->
            err instanceof ServiceNotFoundException
                    && err.receiver == rcv
                    && err.action == action
        }

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
    }

    def 'request throws when service has non-matching type'() {
        given: 'service is registered with PUB_SUB_TOPIC type'
        callDispatcher.ebAddr[srvName] = topicAddr
        callDispatcher.serviceTypes[srvName] = PUB_SUB_TOPIC

        when: 'do REQUEST service'
        def res = callDispatcher.request(rcv, action, args, opts).test().await()

        then: 'WrongServiceTypeException is thrown correctly'
        noExceptionThrown()
        res.assertTerminated()
        res.assertError { err ->
            err instanceof WrongServiceTypeException
                    && err.receiver == rcv
                    && err.action == action
                    && err.expectedType == REQUEST_RESPONSE
                    && err.actualType == PUB_SUB_TOPIC
        }

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
    }

    def 'publish performs correct actions'() {
        setup:
        callDispatcher.ebAddr[srvName] = topicAddr
        callDispatcher.serviceTypes[srvName] = PUB_SUB_TOPIC

        when:
        def res = callDispatcher.publish(rcv, action, args, opts).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        1 * ebMsgCodec.name() >> codec
        1 * eventBus.publish(topicAddr, args, { it instanceof DeliveryOptions && it.codecName == codec })
                >> eventBus
    }

    def 'publish with no delivery options provided sets up codec correctly'() {
        setup:
        callDispatcher.ebAddr[srvName] = topicAddr
        callDispatcher.serviceTypes[srvName] = PUB_SUB_TOPIC

        when:
        def res = callDispatcher.publish(rcv, action, args).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        1 * ebMsgCodec.name() >> codec
        1 * eventBus.publish(topicAddr, args, { it instanceof DeliveryOptions && it.codecName == codec })
                >> eventBus
    }

    def 'publish throws when ebAddr for service is not registered'() {
        given: 'service is not registered'
        callDispatcher.ebAddr.remove(srvName)

        when: 'publish event to unregistered service'
        def res = callDispatcher.publish(rcv, action, args, opts).test().await()

        then: 'ServiceNotFoundException is thrown correctly'
        noExceptionThrown()
        res.assertTerminated()
        res.assertError { err ->
            err instanceof ServiceNotFoundException
                    && err.receiver == rcv
                    && err.action == action
        }

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
    }

    def 'publish throws when service has non-matching type'() {
        given: 'service is registered with CALL type'
        callDispatcher.ebAddr[srvName] = callAddr
        callDispatcher.serviceTypes[srvName] = CALL

        when: 'do PUBLISH event to service'
        def res = callDispatcher.publish(rcv, action, args, opts).test().await()

        then: 'WrongServiceTypeException is thrown correctly'
        noExceptionThrown()
        res.assertTerminated()
        res.assertError { err ->
            err instanceof WrongServiceTypeException
                    && err.receiver == rcv
                    && err.action == action
                    && err.expectedType == PUB_SUB_TOPIC
                    && err.actualType == CALL
        }

        and:
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
    }

    def 'publish with no args delegates correctly with args=null'() {
        setup:
        callDispatcher = Spy(callDispatcher)

        when:
        def res = callDispatcher.publish(rcv, action, opts).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()
        res.assertComplete()
        res.assertNoValues()

        and:
        1 * callDispatcher.publish(rcv, action, null, opts) >> Completable.complete()
    }

    def 'init performs correct actions'() {
        setup:
        callDispatcher.discoveryAnnounceAddress = 'service.discovery.announce'

        when:
        callDispatcher.init()

        then: 'eb msg codec is registered'
        1 * eventBus.registerCodec(ebMsgCodec)

        and: 'is subscribed to discovery events of mindis eb services'
        callDispatcher.discoverySubscription

        and:
        1 * eventBus.consumer(callDispatcher.discoveryAnnounceAddress, _)
        1 * serviceDiscovery.rxGetRecords(_ as Function<Record, Boolean>) >> Single.just([])
    }

    def 'dispose disposes service discovery subscription'() {
        setup:
        def discoverySubscription = Mock(Disposable)
        callDispatcher.discoverySubscription = discoverySubscription

        when:
        callDispatcher.dispose()

        then:
        noExceptionThrown()
        1 * discoverySubscription.dispose()
    }
}
