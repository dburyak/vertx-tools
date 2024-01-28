package com.dburyak.vertx.core.util;

import lombok.Value;

@Value
public class Tuple4<T1, T2, T3, T4> {
    T1 v1;
    T2 v2;
    T3 v3;
    T4 v4;

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 v1, T2 v2, T3 v3, T4 v4) {
        return new Tuple4<>(v1, v2, v3, v4);
    }

    public <T> T getAt(int idx) {
        return switch (idx) {
            case 0 -> (T) v1;
            case 1 -> (T) v2;
            case 2 -> (T) v3;
            case 3 -> (T) v4;
            default -> throw new IndexOutOfBoundsException("Tuple4 does not have an element at index " + idx);
        };
    }
}
