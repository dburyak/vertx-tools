package com.dburyak.vertx.core.deployment.spec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class EventBusVerticle extends Verticle<EventBusVerticle.InAction, EventBusVerticle.OutAction> {
    private final String producer;

    @Override
    public List<String> getAllAddresses() {
        return getInActions().getList().stream()
                .map(a -> a.getFullAddr(getInActions()))
                .collect(Collectors.toList());
    }

    protected EventBusVerticle(EventBusVerticleBuilder<?, ?> builder) {
        super(builder);
        if (builder.producer == null || builder.producer.isBlank()) {
            throw new IllegalStateException("producer must be specified: name=" + getName());
        }
        producer = builder.producer.strip();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    public static class InAction extends com.dburyak.vertx.core.deployment.spec.InAction {

        protected InAction(InActionBuilder<?, ?> builder) {
            super(builder);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    public static class OutAction extends com.dburyak.vertx.core.deployment.spec.OutAction {

        protected OutAction(OutActionBuilder<?, ?> builder) {
            super(builder);
        }
    }
}
