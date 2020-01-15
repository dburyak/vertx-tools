package com.archiuse.mindis.call

/**
 * Verticle service type.
 */
enum ServiceType {

    /**
     * "Call and forget" type of service.
     */
    CALL('mindis.verticle.call'),

    /**
     * "Request-response" type of service.
     */
    REQUEST_RESPONSE('mindis.verticle.request_response'),

    /**
     * "Publish/subscribe" type of service.
     */
    PUB_SUB_TOPIC('mindis.verticle.pub_sub_topic')

    final String typeName

    ServiceType(String typeName) {
        this.typeName = typeName
    }

    static ServiceType byTypeName(String typeName) {
        values().find { it.typeName == typeName }
    }
}
