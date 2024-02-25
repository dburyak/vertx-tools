package com.dburyak.vertx.gcp.pubsub.config;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Duration;

/**
 * PubSub publisher configuration properties.
 */
@ConfigurationProperties("vertx.gcp.pubsub.publisher")
@Getter
public class PubSubPublisherProperties {

    /**
     * Publisher shutdown timeout.
     */
    private final Duration shutdownTimeout;

    @ConfigurationInject
    public PubSubPublisherProperties(
            @Bindable(defaultValue = "30s") @MinDuration("0s") @NotNull Duration shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }
}
