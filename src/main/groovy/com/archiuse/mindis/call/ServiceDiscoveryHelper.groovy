package com.archiuse.mindis.call


import io.vertx.core.json.JsonObject
import io.vertx.servicediscovery.Record

import javax.inject.Singleton

import static CommunicationType.VERTX_EVENT_BUS_P2P
import static CommunicationType.VERTX_EVENT_BUS_TOPIC
import static ServiceType.CALL
import static ServiceType.PUB_SUB_TOPIC
import static ServiceType.REQUEST_RESPONSE

@Singleton
class ServiceDiscoveryHelper {
    private static final String META_KEY_COMM_TYPE = 'comm_type'
    private static final String KEY_METADATA = 'metadata'
    private static final String KEY_TYPE = 'type'


    String getKeyCommType() {
        META_KEY_COMM_TYPE
    }

    String getKeyMetadata() {
        KEY_METADATA
    }

    String getKeyType() {
        KEY_TYPE
    }

    boolean isEventBusCall(Record serviceRecord) {
        def commType = serviceRecord.metadata.getString(keyCommType)
        isEventBusCall serviceRecord.type, commType
    }

    boolean isEventBusCall(JsonObject serviceRecordJson) {
        def commType = serviceRecordJson.getJsonObject(keyMetadata)?.getString(keyCommType)
        isEventBusCall serviceRecordJson.getString(keyType), commType
    }

    boolean isEventBusCall(String serviceType, String commType) {
        serviceType == CALL.typeName
                && commType == VERTX_EVENT_BUS_P2P.typeName
    }

    boolean isEventBusRequestResponse(Record serviceRecord) {
        def commType = serviceRecord.metadata.getString(keyCommType)
        isEventBusRequestResponse serviceRecord.type, commType
    }

    boolean isEventBusRequestResponse(JsonObject serviceRecordJson) {
        def commType = serviceRecordJson.getJsonObject(keyMetadata)?.getString(keyCommType)
        isEventBusRequestResponse serviceRecordJson.getString(keyType), commType
    }

    boolean isEventBusRequestResponse(String type, String commType) {
        type == REQUEST_RESPONSE.typeName
                && commType == VERTX_EVENT_BUS_P2P.typeName
    }

    boolean isEventBusTopic(Record serviceRecord) {
        def commType = serviceRecord.metadata.getString(keyCommType)
        isEventBusTopic serviceRecord.type, commType
    }

    boolean isEventBusTopic(JsonObject serviceRecordJson) {
        def commType = serviceRecordJson.getJsonObject(keyMetadata)?.getString(keyCommType)
        isEventBusTopic serviceRecordJson.getString(keyType), commType
    }

    boolean isEventBusTopic(String type, String commType) {
        type == PUB_SUB_TOPIC.typeName
                && commType == VERTX_EVENT_BUS_TOPIC.typeName
    }

    boolean isEventBusService(Record serviceRecord) {
        isEventBusCall(serviceRecord)
                || isEventBusRequestResponse(serviceRecord)
                || isEventBusTopic(serviceRecord)
    }

    boolean isEventBusService(JsonObject serviceRecordJson) {
        isEventBusCall(serviceRecordJson)
                || isEventBusRequestResponse(serviceRecordJson)
                || isEventBusTopic(serviceRecordJson)
    }
}
