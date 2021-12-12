package com.dburyak.vertx.core.deployment.spec;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Data
@Builder(toBuilder = true)
public class Verticles {

    @Builder.Default
    private final List<Verticle<?, ?>> verticles = Collections.emptyList();

    @Builder.Default
    private final int instances = 1;

    public static class VerticlesBuilder {
        public Verticles build() {
            Objects.requireNonNull(verticles$value);
            if (instances$value <= 0) {
                throw new IllegalStateException("instances must be positive: " + instances$value);
            }

            // check if all verticles have unique names
            var nonUniqueNames = verticles$value.stream()
                    .collect(groupingBy(Verticle::getName, counting()))
                    .entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(toList());
            if (!nonUniqueNames.isEmpty()) {
                throw new IllegalStateException("duplicate verticle names: nonUniqueNames=" + nonUniqueNames);
            }

            // check if all actions have unique addresses
            var nonUniqueAddresses = verticles$value.stream()
                    .flatMap(v -> v.getAllAddresses().stream())
                    .collect(groupingBy(identity(), counting()))
                    .entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (!nonUniqueAddresses.isEmpty()) {
                throw new IllegalStateException("duplicate verticles addresses: nonUniqueAddresses="
                        + nonUniqueAddresses);
            }

            return new Verticles(verticles$value, instances$value);
        }
    }
}
