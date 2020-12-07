package com.dburyak.vertx.core.eventbus;

public class LocalAwareKryoUnsafeMessageCodec extends KryoMessageCodec {

    @Override
    public Object transform(Object data) {
        return data;
    }
}
