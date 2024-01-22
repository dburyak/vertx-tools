package com.dburyak.vertx.gcp.pubsub;

import com.google.pubsub.v1.PubsubMessage;

public record DeliverableMsg(
        PubsubMessage msg,
        Delivery delivery) {}
