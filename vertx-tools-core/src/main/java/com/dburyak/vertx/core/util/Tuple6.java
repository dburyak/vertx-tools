package com.dburyak.vertx.core.util;

import lombok.Value;

@Value
public class Tuple6<T1, T2, T3, T4, T5, T6> {
    T1 v1;
    T2 v2;
    T3 v3;
    T4 v4;
    T5 v5;
    T6 v6;

    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> of(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5,
            T6 v6) {
        return new Tuple6<>(v1, v2, v3, v4, v5, v6);
    }

    public <T> T getAt(int idx) {
        return switch (idx) {
            case 0 -> (T) v1;
            case 1 -> (T) v2;
            case 2 -> (T) v3;
            case 3 -> (T) v4;
            case 4 -> (T) v5;
            case 5 -> (T) v6;
            default -> throw new IndexOutOfBoundsException("Tuple6 does not have an element at index " + idx);
        };
    }
}
