package com.dburyak.vertx.core.deployment.spec;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.dburyak.vertx.core.deployment.spec.Headers.Type.ALL;
import static com.dburyak.vertx.core.deployment.spec.Headers.Type.NONE;

@Data
@Builder(toBuilder = true)
public class Headers {
    private final Type type;

    @Builder.Default
    private final List<CharSequence> include = Collections.emptyList();

    @Builder.Default
    private final List<CharSequence> exclude = Collections.emptyList();

    public static class HeadersBuilder {

        public Headers build() {
            Objects.requireNonNull(type);
            return new Headers(type, include$value, exclude$value);
        }

    }
    public static Headers all() {
        return Headers.builder()
                .type(ALL)
                .build();
    }

    public static Headers none() {
        return Headers.builder()
                .type(NONE)
                .build();
    }

    public enum Type {
        ALL,
        NONE,
        CUSTOM
    }
}
