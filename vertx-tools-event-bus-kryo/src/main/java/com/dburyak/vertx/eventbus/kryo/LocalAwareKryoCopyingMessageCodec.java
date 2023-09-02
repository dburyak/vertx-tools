package com.dburyak.vertx.eventbus.kryo;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

/**
 * Kryo message codec that copies data via copy constructor for local communications. Copying is used to pass data
 * between vertx threads without synchronization. This codec is a safe alternative for
 * {@link com.dburyak.vertx.eventbus.LocalAwareJsonUnsafeMessageCodec}.
 *
 * @param <T> type of message to send
 */
@Singleton
@Slf4j
public class LocalAwareKryoCopyingMessageCodec<T> extends KryoMessageCodecBase<T, T> {

    @SuppressWarnings("unchecked")
    @Override
    public T transform(T data) {
        // copy object via copy constructor
        var dataClass = data.getClass();
        try {
            return (T) dataClass.getDeclaredConstructor(dataClass).newInstance(data);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            log.error("failed to find public copy constructor of data object", e);
            throw new IllegalArgumentException("data object must implement public copy constructor: " + data, e);
        } catch (InvocationTargetException e) {
            log.error("failed to call copy constructor of data object", e);
            throw new RuntimeException(e);
        }
    }
}
