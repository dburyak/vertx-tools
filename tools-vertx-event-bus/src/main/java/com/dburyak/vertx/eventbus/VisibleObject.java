package com.dburyak.vertx.eventbus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisibleObject<T> {
    private volatile T data;

    public static <T> VisibleObject<T> of(T data) {
        return new VisibleObject<>(data);
    }
}
