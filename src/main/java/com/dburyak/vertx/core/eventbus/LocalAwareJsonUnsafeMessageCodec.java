package com.dburyak.vertx.core.eventbus;

public class LocalAwareJsonUnsafeMessageCodec extends JsonMessageCodec {

    @Override
    public Object transform(Object data) {
        return data;
    }
}
