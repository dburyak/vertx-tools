package com.dburyak.vertx.eventbus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper around any object that provides volatile visibility guarantees, but doesn't perform any copying. Intended to
 * be used only for EventBus messages and codecs to avoid unnecessary data copying for local EventBus messages.
 *
 * @param <T> type of wrapped object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsafeMessage<T> {
    private volatile T data;

    /**
     * Static factory method to create message object.
     *
     * @param data wrapped object
     * @param <T> type of wrapped object
     *
     * @return new message object instance
     */
    public static <T> UnsafeMessage<T> of(T data) {
        return new UnsafeMessage<>(data);
    }
}
