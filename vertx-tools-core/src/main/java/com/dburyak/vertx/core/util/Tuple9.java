package com.dburyak.vertx.core.util;

import lombok.Value;

@Value
public class Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> {
    T1 v1;
    T2 v2;
    T3 v3;
    T4 v4;
    T5 v5;
    T6 v6;
    T7 v7;
    T8 v8;
    T9 v9;

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T1 v1, T2 v2,
            T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9) {
        return new Tuple9<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
    }

    public <T> T getAt(int idx) {
        return switch (idx) {
            case 0 -> (T) v1;
            case 1 -> (T) v2;
            case 2 -> (T) v3;
            case 3 -> (T) v4;
            case 4 -> (T) v5;
            case 5 -> (T) v6;
            case 6 -> (T) v7;
            case 7 -> (T) v8;
            case 8 -> (T) v9;
            default -> throw new IndexOutOfBoundsException("Tuple9 does not have an element at index " + idx);
        };
    }
}
