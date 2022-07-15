package com.dburyak.vertx.eventbus;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Singleton
@Slf4j
public class LocalAwareJsonCopyingMessageCodec extends JsonMessageCodec {

    @Override
    public Object transform(Object data) {
        // copy object via copy constructor
        var dataClass = data.getClass();
        try {
            return dataClass.getDeclaredConstructor(data.getClass()).newInstance(data);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                 | NoSuchMethodException e) {
            log.error("failed to call copy constructor of data object", e);
            throw new IllegalArgumentException("data object must implement copy constructor: " + data, e);
        }
    }
}
