package com.dburyak.vertx.eventbus.kryo;

import com.dburyak.vertx.eventbus.VisibleObject;
import jakarta.inject.Singleton;

/**
 * Kryo message codec that passes data without copying for local communications. This codec passes data as is between
 * sender and receiver threads with volatile visibility guarantees only.
 *
 * @param <T> type of message to send
 */
@Singleton
public class LocalAwareKryoUnsafeMessageCodec<T> extends KryoMessageCodecBase<VisibleObject<T>, T> {

    @Override
    public T transform(VisibleObject<T> sentData) {
        return sentData.getData();
    }
}
