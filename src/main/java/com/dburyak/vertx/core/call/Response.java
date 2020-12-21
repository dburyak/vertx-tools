package com.dburyak.vertx.core.call;

import io.vertx.reactivex.core.MultiMap;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Response {

    /**
     * Response message data.
     */
    Object msg;

    /**
     * Response headers.
     */
    MultiMap headers;
}
