package com.dburyak.vertx.core.util;

import lombok.Value;

@Value
public class Tuple2<T1, T2> {
    T1 v1;
    T2 v2;

    public static <T1, T2> Tuple2<T1, T2> of(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }

    public <T> T getAt(int idx) {
        return switch (idx) {
            case 0 -> (T) v1;
            case 1 -> (T) v2;
            default -> throw new IndexOutOfBoundsException("Tuple2 does not have an element at index " + idx);
        };
    }
}
