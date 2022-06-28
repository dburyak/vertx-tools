package com.dburyak.vertx.core.deployer.deployment.spec;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public abstract class InAction {
    private final String name;
    private final String addr;
    private final String baseAddr;

    public String getFullAddr(InActions<?> inActions) {
        return ((baseAddr != null) ? baseAddr : inActions.getBaseAddr()) + addr;
    }

    protected InAction(InActionBuilder<?, ?> builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new IllegalStateException("in action name must be specified: baseAddr="
                    + builder.baseAddr + ", addr=" + builder.addr);
        }
        name = builder.name.strip();
        addr = (builder.addr != null && !builder.addr.isBlank()) ? builder.addr.strip() : name;
        baseAddr = (builder.baseAddr != null) ? builder.baseAddr.strip() : null;
    }
}
