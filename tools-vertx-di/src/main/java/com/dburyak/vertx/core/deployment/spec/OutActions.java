package com.dburyak.vertx.core.deployment.spec;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class OutActions<OUT extends OutAction> {

    @Builder.Default
    private final List<OUT> list = Collections.emptyList();

    public static <T extends OutAction> OutActions<T> none() {
        return OutActions.<T>builder().build();
    }

    public static class OutActionsBuilder<OUT extends OutAction> {
        public OutActions<OUT> build() {
            return new OutActions<>(list$value);
        }
    }
}
