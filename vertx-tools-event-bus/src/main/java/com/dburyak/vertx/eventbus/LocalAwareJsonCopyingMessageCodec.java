package com.dburyak.vertx.eventbus;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

/**
 * Json codec that uses vertx json (based on jackson) for sending data over the wire, and copies objects via
 * copy-constructor when sending data locally in the same jvm.
 * Is safe for cases when object sent is mutable and can be modified by sender after receiver received it.
 */
@Singleton
@Slf4j
public class LocalAwareJsonCopyingMessageCodec<T> extends JsonMessageCodec<T, T> {

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
