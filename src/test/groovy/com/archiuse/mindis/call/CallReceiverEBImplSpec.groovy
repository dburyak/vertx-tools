package com.archiuse.mindis.call

import com.archiuse.mindis.VertxRxJavaSpec
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.eventbus.EventBus
import io.vertx.reactivex.core.eventbus.MessageConsumer
import io.vertx.reactivex.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.Record

import static com.archiuse.mindis.call.CommunicationType.VERTX_EVENT_BUS_P2P
import static com.archiuse.mindis.call.CommunicationType.VERTX_EVENT_BUS_TOPIC
import static com.archiuse.mindis.call.ServiceType.CALL
import static com.archiuse.mindis.call.ServiceType.PUB_SUB_TOPIC
import static com.archiuse.mindis.call.ServiceType.REQUEST_RESPONSE

class CallReceiverEBImplSpec extends VertxRxJavaSpec {
    CallReceiverEBImpl callReceiver = new CallReceiverEBImpl()

    EBServiceImplHelper ebServiceImplHelper = Mock(EBServiceImplHelper)
    EventBus eventBus = Mock(EventBus)
    MessageConsumer messageConsumer = Mock(MessageConsumer)
    ServiceDiscovery serviceDiscovery = Mock(ServiceDiscovery)
    ServiceDiscoveryHelper serviceDiscoveryHelper = Mock(ServiceDiscoveryHelper)

    def defaultMapAsType

    void setup() {
        callReceiver.ebServiceImplHelper = ebServiceImplHelper
        callReceiver.eventBus = eventBus
        callReceiver.serviceDiscovery = serviceDiscovery
        callReceiver.serviceDiscoveryHelper = serviceDiscoveryHelper
        defaultMapAsType = Map.metaClass.getMetaMethod('asType', [Class] as Class[])
        Map.metaClass.asType = { Class type ->
            if (JsonObject.isAssignableFrom(type)) {
                new JsonObject(delegate)
            } else {
                defaultMapAsType.invoke(delegate, type)
            }
        }
    }

    void cleanup() {
        Map.metaClass.asType = defaultMapAsType
    }

    def 'onCall registers eb msg consumer correctly'() {
        setup:
        def rcv = 'receiver'
        def action = 'action'
        def doOnCall = { /* no action */ }
        def srvName = 'receiver/action'
        def ebAddr = 'call:receiver:action'
        def keyEbAddr = 'eb_addr'
        def keyCommType = 'comm_type'
        def srvRegId = 'ServiceDiscoveryRegId'

        when:
        def res = callReceiver.onCall(rcv, action, doOnCall).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()

        and:
        1 * ebServiceImplHelper.buildEbServiceAddr(rcv, action, CALL) >> ebAddr
        1 * eventBus.consumer(ebAddr, _) >> messageConsumer
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        _ * ebServiceImplHelper.getProperty('keyEbAddr') >> keyEbAddr
        _ * serviceDiscoveryHelper.getProperty('keyCommType') >> keyCommType
        1 * serviceDiscovery.rxPublish(_) >> Single.just(new Record().tap {
            it.name = srvName
            it.type = CALL.typeName
            location = [(keyEbAddr): ebAddr]
            metadata = [(keyCommType): VERTX_EVENT_BUS_P2P]
            registration = srvRegId
        })
        1 * ebServiceImplHelper.toDisposable(messageConsumer, srvRegId) >> ({} as Disposable)
    }

    def 'onRequest registers msg consumer correctly'() {
        setup:
        def rcv = 'receiver'
        def action = 'action'
        def doOnRequest = { Maybe.empty() }
        def srvName = 'receiver/action'
        def ebAddr = 'request-response:receiver:action'
        def keyEbAddr = 'eb_addr'
        def keyCommType = 'comm_type'
        def srvRegId = 'ServiceDiscoveryRegId'

        when:
        def res = callReceiver.onRequest(rcv, action, doOnRequest).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()

        and:
        1 * ebServiceImplHelper.buildEbServiceAddr(rcv, action, REQUEST_RESPONSE) >> ebAddr
        1 * eventBus.consumer(ebAddr, _) >> messageConsumer
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        _ * ebServiceImplHelper.getProperty('keyEbAddr') >> keyEbAddr
        _ * serviceDiscoveryHelper.getProperty('keyCommType') >> keyCommType
        1 * serviceDiscovery.rxPublish(_) >> Single.just(new Record().tap {
            it.name = srvName
            it.type = REQUEST_RESPONSE.typeName
            location = [(keyEbAddr): ebAddr]
            metadata = [(keyCommType): VERTX_EVENT_BUS_P2P]
            registration = srvRegId
        })
        1 * ebServiceImplHelper.toDisposable(messageConsumer, srvRegId) >> ({} as Disposable)
    }

    def 'subscribe registers eb topic consumer correctly'() {
        setup:
        def rcv = 'receiver'
        def action = 'action'
        def doOnEvent = { /* no action */ }
        def srvName = 'receiver/action'
        def ebAddr = 'topic:receiver:action'
        def keyEbAddr = 'eb_addr'
        def keyCommType = 'comm_type'
        def srvRegId = 'ServiceDiscoveryRegId'

        when:
        def res = callReceiver.subscribe(rcv, action, doOnEvent).test().await()

        then:
        noExceptionThrown()
        res.assertNoErrors()

        and:
        1 * ebServiceImplHelper.buildEbServiceAddr(rcv, action, PUB_SUB_TOPIC) >> ebAddr
        1 * eventBus.consumer(ebAddr, _) >> messageConsumer
        1 * ebServiceImplHelper.buildEbServiceName(rcv, action) >> srvName
        _ * ebServiceImplHelper.getProperty('keyEbAddr') >> keyEbAddr
        _ * serviceDiscoveryHelper.getProperty('keyCommType') >> keyCommType
        1 * serviceDiscovery.rxPublish(_) >> Single.just(new Record().tap {
            it.name = srvName
            it.type = PUB_SUB_TOPIC.typeName
            location = [(keyEbAddr): ebAddr]
            metadata = [(keyCommType): VERTX_EVENT_BUS_TOPIC]
            registration = srvRegId
        })
        1 * ebServiceImplHelper.toDisposable(messageConsumer, srvRegId) >> ({} as Disposable)
    }
}
