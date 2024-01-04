package com.dburyak.vertx.gcp.pubsub;

import com.google.cloud.pubsub.v1.Subscriber;

public class PubSub {
    private final PubSubProperties properties;
    private final PubSubSubscriberProperties subscriberProperties;

    public PubSub(PubSubProperties properties, PubSubSubscriberProperties subscriberProperties, Subscriber subscriber) {
        this.properties = properties;
        this.subscriberProperties = subscriberProperties;
    }

    // TODO: implement - make it similar to PubSubTemplate from spring-cloud-gcp-pubsub
}
