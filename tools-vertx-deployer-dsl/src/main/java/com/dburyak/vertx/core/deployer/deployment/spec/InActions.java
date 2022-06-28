package com.dburyak.vertx.core.deployer.deployment.spec;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class InActions<IN extends InAction> {

    @Builder.Default
    private final List<IN> list = Collections.emptyList();

    @Builder.Default
    private final String baseAddr = "";

    public static <T extends InAction> InActions<T> none() {
        return InActions.<T>builder().build();
    }

    public static class InActionsBuilder<IN extends InAction> {
        public InActions<IN> build() {
            if (list$value == null) {
                throw new IllegalStateException("in actions list can not be null");
            }
            return new InActions<>(list$value, baseAddr$value.strip());
        }
    }
}
