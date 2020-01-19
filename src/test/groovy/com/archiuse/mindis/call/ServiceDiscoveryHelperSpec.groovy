package com.archiuse.mindis.call

import io.vertx.core.json.JsonObject
import io.vertx.servicediscovery.Record
import spock.lang.Specification

import static com.archiuse.mindis.call.CommunicationType.VERTX_EVENT_BUS_P2P
import static com.archiuse.mindis.call.CommunicationType.VERTX_EVENT_BUS_TOPIC
import static com.archiuse.mindis.call.ServiceType.CALL
import static com.archiuse.mindis.call.ServiceType.PUB_SUB_TOPIC
import static com.archiuse.mindis.call.ServiceType.REQUEST_RESPONSE

class ServiceDiscoveryHelperSpec extends Specification {
    ServiceDiscoveryHelper serviceDiscoveryHelper = Spy(ServiceDiscoveryHelper)

    def 'all overloads delegate correctly'() {
        given:
        def t = 'type_name'
        def c = 'comm_type_name'
        def rec = new Record().tap { type = t; metadata.put(serviceDiscoveryHelper.keyCommType, c) }
        def recJson = new JsonObject(type: t, metadata: [(serviceDiscoveryHelper.keyCommType): c])

        when:
        serviceDiscoveryHelper.isEventBusCall(rec)
        serviceDiscoveryHelper.isEventBusCall(recJson)
        serviceDiscoveryHelper.isEventBusRequestResponse(rec)
        serviceDiscoveryHelper.isEventBusRequestResponse(recJson)
        serviceDiscoveryHelper.isEventBusTopic(rec)
        serviceDiscoveryHelper.isEventBusTopic(recJson)

        then:
        2 * serviceDiscoveryHelper.isEventBusCall(t, c)
        2 * serviceDiscoveryHelper.isEventBusRequestResponse(t, c)
        2 * serviceDiscoveryHelper.isEventBusTopic(t, c)
    }

    def 'isEventBusCall is detected correctly'() {
        expect:
        serviceDiscoveryHelper.isEventBusCall(serviceType.typeName, commType.typeName) == result

        where:
        serviceType      | commType              || result
        CALL             | VERTX_EVENT_BUS_P2P   || true
        CALL             | VERTX_EVENT_BUS_TOPIC || false
        REQUEST_RESPONSE | VERTX_EVENT_BUS_P2P   || false
        REQUEST_RESPONSE | VERTX_EVENT_BUS_TOPIC || false
        PUB_SUB_TOPIC    | VERTX_EVENT_BUS_P2P   || false
        PUB_SUB_TOPIC    | VERTX_EVENT_BUS_TOPIC || false
    }

    def 'isEventBusRequest is detected correctly'() {
        expect:
        serviceDiscoveryHelper.isEventBusRequestResponse(serviceType.typeName, commType.typeName) == result

        where:
        serviceType      | commType              || result
        CALL             | VERTX_EVENT_BUS_P2P   || false
        CALL             | VERTX_EVENT_BUS_TOPIC || false
        REQUEST_RESPONSE | VERTX_EVENT_BUS_P2P   || true
        REQUEST_RESPONSE | VERTX_EVENT_BUS_TOPIC || false
        PUB_SUB_TOPIC    | VERTX_EVENT_BUS_P2P   || false
        PUB_SUB_TOPIC    | VERTX_EVENT_BUS_TOPIC || false
    }

    def 'isEventBusTopic is detected correctly'() {
        expect:
        serviceDiscoveryHelper.isEventBusTopic(serviceType.typeName, commType.typeName) == result

        where:
        serviceType      | commType              || result
        CALL             | VERTX_EVENT_BUS_P2P   || false
        CALL             | VERTX_EVENT_BUS_TOPIC || false
        REQUEST_RESPONSE | VERTX_EVENT_BUS_P2P   || false
        REQUEST_RESPONSE | VERTX_EVENT_BUS_TOPIC || false
        PUB_SUB_TOPIC    | VERTX_EVENT_BUS_P2P   || false
        PUB_SUB_TOPIC    | VERTX_EVENT_BUS_TOPIC || true
    }

    def 'isEventBusService delegates correctly'() {
        given:
        def rec = new Record()
        def recJson = new JsonObject(type: 't', metadata: [(serviceDiscoveryHelper.keyCommType): 'c'])

        when:
        def res1 = serviceDiscoveryHelper.isEventBusService(rec)
        def res2 = serviceDiscoveryHelper.isEventBusService(recJson)

        then:
        !res1
        !res2

        and:
        1 * serviceDiscoveryHelper.isEventBusCall(rec) >> false
        1 * serviceDiscoveryHelper.isEventBusRequestResponse(rec) >> false
        1 * serviceDiscoveryHelper.isEventBusTopic(rec) >> false

        and:
        1 * serviceDiscoveryHelper.isEventBusCall(recJson) >> false
        1 * serviceDiscoveryHelper.isEventBusRequestResponse(recJson) >> false
        1 * serviceDiscoveryHelper.isEventBusTopic(recJson) >> false
    }
}
