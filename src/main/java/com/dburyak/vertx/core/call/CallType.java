package com.dburyak.vertx.core.call;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum CallType {

    /**
     * "Call and forget" point-to-point type of call.
     */
    NOTIFICATION("verticle.call.type.notification"),

    /**
     * "Request-response" point-to-point type of call.
     */
    REQUEST_RESPONSE("verticle.call.type.request_response"),

    /**
     * "Publish/subscribe" one-to-many type of call.
     */
    PUB_SUB_TOPIC("verticle.call.type.pub_sub_topic");

    private final String typeName;

    static CallType ofTypeName(String typeName) {
        return Arrays.stream(values())
                .filter(t -> t.typeName.equals(typeName))
                .findFirst().orElse(null);
    }
}
