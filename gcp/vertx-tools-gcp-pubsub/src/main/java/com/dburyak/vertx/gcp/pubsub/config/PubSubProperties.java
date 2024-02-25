package com.dburyak.vertx.gcp.pubsub.config;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;

/**
 * PubSub configuration properties.
 */
@ConfigurationProperties("vertx.gcp.pubsub")
@Getter
public class PubSubProperties {

    /**
     * PubSub publisher configuration properties.
     */
    private final PubSubPublisherProperties publisherProperties;

    /**
     * PubSub subscriber configuration properties.
     */
    private final PubSubSubscriberProperties subscriberProperties;

    @ConfigurationInject
    public PubSubProperties(
            PubSubPublisherProperties publisherProperties,
            PubSubSubscriberProperties subscriberProperties) {
        this.publisherProperties = publisherProperties;
        this.subscriberProperties = subscriberProperties;
    }
}
