package com.dburyak.vertx.core.util;

import lombok.Value;

@Value
public class Tuple3<T1, T2, T3> {
    T1 v1;
    T2 v2;
    T3 v3;

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 v1, T2 v2, T3 v3) {
        return new Tuple3<>(v1, v2, v3);
    }

    public <T> T getAt(int idx) {
        return switch (idx) {
            case 0 -> (T) v1;
            case 1 -> (T) v2;
            case 2 -> (T) v3;
            default -> throw new IndexOutOfBoundsException("Tuple3 does not have an element at index " + idx);
        };
    }
}
