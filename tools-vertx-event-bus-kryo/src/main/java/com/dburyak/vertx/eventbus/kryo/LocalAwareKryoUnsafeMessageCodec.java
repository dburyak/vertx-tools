package com.dburyak.vertx.eventbus.kryo;

import com.dburyak.vertx.eventbus.VisibleObject;
import jakarta.inject.Singleton;

@Singleton
public class LocalAwareKryoUnsafeMessageCodec<T> extends KryoMessageCodecBase<VisibleObject<T>, T> {

    @Override
    public T transform(VisibleObject<T> sentData) {
        return sentData.getData();
    }
}
