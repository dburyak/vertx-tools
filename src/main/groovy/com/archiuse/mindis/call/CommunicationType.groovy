package com.archiuse.mindis.call

/**
 * Type of communication technology.
 */
enum CommunicationType {
    VERTX_EVENT_BUS_P2P('VERTX_EVENT_BUS_P2P'),
    VERTX_EVENT_BUS_TOPIC('VERTX_EVENT_BUS_TOPIC')

    final String typeName

    CommunicationType(String typeName) {
        this.typeName = typeName
    }
}
