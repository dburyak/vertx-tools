package com.dburyak.vertx.eventbus.kryo;

public class LocalAwareKryoUnsafeMessageCodec extends KryoMessageCodecBase {

    /**
     * Volatile ref to provide safe publishing of the data object, so the receiver could see the state "data" had
     * before it's being sent over EB. This doesn't solve concurrent access though, only visibility.
     */
    private volatile Object dataRef;

    @Override
    public Object transform(Object data) {
        dataRef = data;
        return dataRef;
    }
}
