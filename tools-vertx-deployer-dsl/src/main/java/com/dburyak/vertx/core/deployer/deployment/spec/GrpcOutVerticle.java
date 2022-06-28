package com.dburyak.vertx.core.deployer.deployment.spec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class GrpcOutVerticle extends Verticle {

    protected GrpcOutVerticle(GrpcOutVerticleBuilder<?, ?> builder) {
        super(builder);
        throw new AssertionError("not implemented");
    }

    @Override
    public List<String> getAllAddresses() {
        throw new AssertionError("not implemented");
    }
}
