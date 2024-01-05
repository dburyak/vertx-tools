package com.dburyak.vertx.gcp.pubsub;

import com.dburyak.vertx.gcp.pubsub.config.PubSubProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;

@Factory
public class PubSubFactory {

    @Singleton
    @Requires(missingBeans = PubSub.class)
    public PubSub pubsub(Vertx vertx, PubSubProperties pubSubProperties) {
        return new PubSubImpl(vertx, pubSubProperties);
    }
}
