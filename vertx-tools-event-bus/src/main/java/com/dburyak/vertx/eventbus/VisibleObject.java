package com.dburyak.vertx.eventbus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Visible object is a wrapper around any object that provides volatile visibility guarantees. Intended to be used only
 * for EventBus messages and codecs to avoid unnecessary data copying for local EventBus messages.
 *
 * @param <T> type of wrapped object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisibleObject<T> {
    private volatile T data;

    /**
     * Static factory method to create visible object.
     *
     * @param data wrapped object
     * @param <T> type of wrapped object
     *
     * @return new visible object instance
     */
    public static <T> VisibleObject<T> of(T data) {
        return new VisibleObject<>(data);
    }
}
