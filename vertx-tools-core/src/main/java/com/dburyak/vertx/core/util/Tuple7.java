package com.dburyak.vertx.core.util;

import lombok.Value;

@Value
public class Tuple7<T1, T2, T3, T4, T5, T6, T7> {
    T1 v1;
    T2 v2;
    T3 v3;
    T4 v4;
    T5 v5;
    T6 v6;
    T7 v7;

    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> of(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5,
            T6 v6, T7 v7) {
        return new Tuple7<>(v1, v2, v3, v4, v5, v6, v7);
    }
}
