package com.dburyak.vertx.eventbus;

import jakarta.inject.Singleton;

/**
 * Json codec that uses vertx json (based on jackson) for sending data over the wire, and passes object as-is with
 * cross-thread visibility guarantees when sending data locally in the same jvm. Can't be used for cases when sent
 * object is mutable and sender can modify it after it is received by receiver.
 *
 * @param <T> payload message type
 */
@Singleton
public class LocalAwareJsonUnsafeVisibleMessageCodec<T> extends JsonMessageCodec<UnsafeMessage<T>, T> {

    @Override
    public T transform(UnsafeMessage<T> unsafeMessage) {
        // forces volatile write on sender thread and volatile read on receiver side, which guarantees visibility
        return unsafeMessage.getData();
    }
}
