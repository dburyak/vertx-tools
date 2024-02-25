package com.dburyak.vertx.gcp.pubsub.config;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Duration;

/**
 * PubSub subscriber configuration properties.
 */
@ConfigurationProperties("vertx.gcp.pubsub.subscriber")
@Getter
public class PubSubSubscriberProperties {

    /**
     * Subscriber shutdown timeout.
     */
    @MinDuration("0s")
    private final Duration shutdownTimeout;

    @ConfigurationInject
    public PubSubSubscriberProperties(
            @Bindable(defaultValue = "30s") @MinDuration("0s") @NotNull Duration shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }
}
