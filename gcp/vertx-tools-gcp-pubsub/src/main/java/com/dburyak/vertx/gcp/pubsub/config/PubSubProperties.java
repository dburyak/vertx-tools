package com.dburyak.vertx.gcp.pubsub.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import jakarta.inject.Inject;
import lombok.Data;

/**
 * PubSub configuration properties.
 */
@ConfigurationProperties("vertx.gcp.pubsub")
@Data
public class PubSubProperties {

    /**
     * PubSub publisher configuration properties.
     */
    private PubSubPublisherProperties publisherProperties;

    /**
     * PubSub subscriber configuration properties.
     */
    private PubSubSubscriberProperties subscriberProperties;

    /**
     * Set publisher properties.
     *
     * @param publisherProperties publisher properties
     */
    @Inject
    public void setPublisherProperties(PubSubPublisherProperties publisherProperties) {
        this.publisherProperties = publisherProperties;
    }

    /**
     * Set subscriber properties.
     *
     * @param subscriberProperties subscriber properties
     */
    @Inject
    public void setSubscriberProperties(PubSubSubscriberProperties subscriberProperties) {
        this.subscriberProperties = subscriberProperties;
    }
}
