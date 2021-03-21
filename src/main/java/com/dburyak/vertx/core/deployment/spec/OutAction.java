package com.dburyak.vertx.core.deployment.spec;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public abstract class OutAction {
    private static final Boolean LOCAL_ONLY_DEFAULT = false;

    private final String name;
    private final String action;
    private final Boolean localOnly;

    protected OutAction(OutActionBuilder<?, ?> builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new IllegalStateException("out action name must be specified");
        }
        name = builder.name.strip();
        action = (builder.action != null && !builder.action.isBlank()) ? builder.action.strip() : name;
        localOnly = (builder.localOnly != null) ? builder.localOnly : LOCAL_ONLY_DEFAULT;
    }
}
