package com.dburyak.vertx.health;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Secondary;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.healthchecks.HealthChecks;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.healthchecks.HealthChecks;

@Factory
@Secondary
public class HealthChecksFactory {

    @Prototype
    @Secondary
    public HealthChecks healthChecks(Vertx vertx) {
        return HealthChecks.create(vertx);
    }
}
