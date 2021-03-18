package com.dburyak.vertx.core;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Secondary;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.healthchecks.HealthChecks;

@Factory
@Secondary
public class HealthChecksFactory {

    @Prototype
    @Secondary
    public HealthChecks healthChecks(Vertx vertx) {
        return HealthChecks.create(vertx);
    }
}
