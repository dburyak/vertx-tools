package com.dburyak.vertx.eventbus.kryo;

import com.dburyak.vertx.eventbus.UnsafeMessage;
import jakarta.inject.Singleton;

/**
 * Kryo message codec that passes data without copying for local communications. This codec passes data as is between
 * sender and receiver threads with volatile visibility guarantees only.
 *
 * @param <T> payload message type
 */
@Singleton
public class LocalAwareKryoUnsafeVisibleMessageCodec<T> extends KryoMessageCodecBase<UnsafeMessage<T>, T> {

    @Override
    public T transform(UnsafeMessage<T> unsafeMessage) {
        // forces volatile write on sender thread and volatile read on receiver side, which guarantees visibility
        return unsafeMessage.getData();
    }
}
