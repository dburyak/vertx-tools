package com.dburyak.vertx.core.deployment.spec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class GrpcOutVerticle extends Verticle {
    public GrpcOutVerticle() {
        throw new AssertionError("not implemented");
    }
}
