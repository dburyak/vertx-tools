package com.dburyak.vertx.core.deployment.spec;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class EventBusVerticle extends Verticle {
    private static final String BASE_ADDR_DEFAULT = "";
    private static final List<Action> ACTIONS_DEFAULT = Collections.emptyList();

    private final String producer;
    private final String baseAddr;
    private final List<Action> actions;

    @Override
    public List<String> getAllAddresses() {
        return actions.stream()
                .map(a -> a.getFullAddr(this))
                .collect(Collectors.toList());
    }

    protected EventBusVerticle(EventBusVerticleBuilder<?, ?> builder) {
        super(builder);
        if (builder.producer == null || builder.producer.isBlank()) {
            throw new IllegalStateException("producer must be specified: name=" + getName());
        }
        producer = builder.producer.strip();
        baseAddr = (builder.baseAddr != null) ? builder.baseAddr.strip() : BASE_ADDR_DEFAULT;
        actions = (builder.actions != null) ? builder.actions : ACTIONS_DEFAULT;
    }

    @Data
    @Builder(toBuilder = true)
    public static class Action {
        private final String name;
        private final String addr;
        private final String baseAddr;

        public String getFullAddr(EventBusVerticle parentVerticle) {
            return ((baseAddr != null) ? baseAddr : parentVerticle.baseAddr) + addr;
        }

        public static class ActionBuilder {
            public Action build() {
                if (name == null || name.isBlank()) {
                    throw new IllegalStateException("event bus verticle action name must be specified: baseAddr="
                            + baseAddr + ", addr=" + addr);
                }
                var effectiveAddr = (addr != null && !addr.isBlank()) ? addr : name;
                var effectiveBaseAddr = (baseAddr != null) ? baseAddr.strip() : null;
                return new Action(name.strip(), effectiveAddr.strip(), effectiveBaseAddr);
            }
        }
    }
}
