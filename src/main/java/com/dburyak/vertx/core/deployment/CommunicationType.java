package com.dburyak.vertx.core.deployment;

public enum CommunicationType {

    /**
     * "Call and forget" point-to-point type of call.
     */
    NOTIFICATION,

    /**
     * "Request-response" point-to-point type of call.
     */
    REQUEST_RESPONSE,

    /**
     * "Publish/subscribe" one-to-many type of call.
     */
    PUB_SUB_TOPIC
}
