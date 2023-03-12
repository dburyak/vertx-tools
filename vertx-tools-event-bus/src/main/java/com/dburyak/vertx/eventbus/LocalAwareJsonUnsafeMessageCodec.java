package com.dburyak.vertx.eventbus;

import jakarta.inject.Singleton;

/**
 * Json codec that uses vertx json (based on jackson) for sending data over the wire, and passes object as-is with
 * cross-thread visibility guarantees when sending data locally in the same jvm.
 * Can't be used for cases when sent object is mutable and sender can modify it after it is received by receiver.
 */
@Singleton
public class LocalAwareJsonUnsafeMessageCodec<T> extends JsonMessageCodec<VisibleObject<T>, T> {

    @Override
    public T transform(VisibleObject<T> sentData) {
        return sentData.getData();
    }
}
