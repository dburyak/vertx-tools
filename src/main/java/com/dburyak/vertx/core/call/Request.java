package com.dburyak.vertx.core.call;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder(toBuilder = true)
public class Request<T> {

    /**
     * Action id.
     */
    private String action;

    /**
     * Request data.
     */
    private T msg;

    /**
     * Request arguments. Correspond to http uri query parameters.
     */
    private Object args;

    /**
     * Request delivery options. Headers (if present) in delivery options take precedence over {@link #headers}.
     */
    private DeliveryOptions deliveryOptions;

    /**
     * Request headers. Headers in {@link #deliveryOptions} if specified take precedence over this property.
     */
    private MultiMap headers;

    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    private Message<T> ebMsg;
}
