package com.dburyak.vertx.core.call;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.MultiMap;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Request {

    /**
     * Action id.
     */
    String action;

    /**
     * Request data.
     */
    Object msg;

    /**
     * Request arguments. Correspond to http uri query parameters.
     */
    Object args;

    /**
     * Request delivery options. Headers (if present) in delivery options take precedence over {@link #headers}.
     */
    DeliveryOptions deliveryOptions;

    /**
     * Request headers. Headers in {@link #deliveryOptions} if specified take precedence over this property.
     */
    MultiMap headers;
}
