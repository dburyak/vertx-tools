package com.dburyak.vertx.gcp.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumerWithResponse;
import com.google.pubsub.v1.PubsubMessage;

public record AckMsg(
        PubsubMessage msg,
        AckReplyConsumerWithResponse ack) {}
