package com.dburyak.vertx.eventbus.kryo;

import jakarta.inject.Singleton;

/**
 * Kryo message codec that does not transform message for local communications. This codec does not provide any
 * guarantees about message visibility, nor performs defensive copying. It is suitable only for immutable objects.
 *
 * @param <T> type of message to send
 */
@Singleton
public class LocalAwareKryoUnsafeMessageCodec<T> extends KryoMessageCodecBase<T, T> {

    @Override
    public T transform(T message) {
        return message;
    }
}
