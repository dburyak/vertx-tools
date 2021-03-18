package com.dburyak.vertx.core.deployment.spec;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static com.dburyak.vertx.core.deployment.spec.VerticleType.VERTX_EVENT_BUS;

@Data
@SuperBuilder(toBuilder = true)
public abstract class Verticle {
    private static final VerticleType TYPE_DEFAULT = VERTX_EVENT_BUS;
    private static final int INSTANCES_DEFAULT = 1;

    private final String name;
    private final VerticleType type;
    private final int instances;

    public abstract List<String> getAllAddresses();

    protected Verticle(VerticleBuilder<?, ?> builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new IllegalStateException("verticle name must be specified: type=" + builder.type);
        }
        name = builder.name.strip();
        type = (builder.type != null) ? builder.type : TYPE_DEFAULT;
        if (builder.instances < 0) {
            throw new IllegalStateException("instances must be positive: name=" + name
                    + ", instances=" + builder.instances);
        }
        instances = (builder.instances > 0) ? builder.instances : INSTANCES_DEFAULT;
    }
}
