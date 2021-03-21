package com.dburyak.vertx.core.deployment.spec;

import com.dburyak.vertx.core.VerticleProducer;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static com.dburyak.vertx.core.deployment.spec.VerticleType.VERTX_EVENT_BUS;

@Data
@SuperBuilder(toBuilder = true)
public abstract class Verticle<IN extends InAction, OUT extends OutAction> {
    private static final VerticleType TYPE_DEFAULT = VERTX_EVENT_BUS;
    private static final int INSTANCES_DEFAULT = 1;

    private final String name;
    private final VerticleType type;
    private final int instances;
    private final InActions<IN> inActions;
    private final OutActions<OUT> outActions;

    public abstract List<String> getAllAddresses();

    public abstract <T extends VerticleProducer<T>> List<com.dburyak.vertx.core.VerticleProducer<T>> createBySpec(
            Verticles verticles);

    protected Verticle(VerticleBuilder<IN, OUT, ?, ?> builder) {
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
        inActions = (builder.inActions != null) ? builder.inActions : inActionsDefault();
        outActions = (builder.outActions != null) ? builder.outActions : outActionsDefault();
    }

    private static <T extends InAction> InActions<T> inActionsDefault() {
        return InActions.none();
    }

    private static <T extends OutAction> OutActions<T> outActionsDefault() {
        return OutActions.none();
    }
}
