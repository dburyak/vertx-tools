package com.dburyak.vertx.gcp.pubsub.config;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.time.Duration;

/**
 * PubSub subscriber configuration properties.
 */
@ConfigurationProperties("vertx.gcp.pubsub.subscriber")
@Data
public class PubSubSubscriberProperties {

    /**
     * Subscriber shutdown timeout.
     */
    @MinDuration("0s")
    private Duration shutdownTimeout = Duration.ofSeconds(30);
}
