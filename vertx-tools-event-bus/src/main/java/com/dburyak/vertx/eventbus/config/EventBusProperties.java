package com.dburyak.vertx.eventbus.config;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;
import lombok.Getter;

import java.util.List;

/**
 * EventBus configuration.
 */
@ConfigurationProperties("vertx.event-bus")
@Getter
public class EventBusProperties {

    /**
     * EventBus message codecs configurations.
     */
    private final List<CodecProperties> codecs;

    /**
     * Whether to fail application startup process if error happened while loading/registering codec. Setting this
     * option to true allows to fail fast if any codec is misconfigured. Otherwise, such erroneous codec will be
     * skipped.
     */
    private final boolean failOnCodecError;

    /**
     * Whether to fail application startup process if error happened while loading/registering codec. Setting this
     * option to true allows to fail fast if any codec is misconfigured. Otherwise, such erroneous codec will be
     * skipped.
     *
     * @return whether to fail application startup process if error happened while loading/registering codec
     */
    public boolean shouldFailOnCodecError() {
        return failOnCodecError;
    }

    @ConfigurationInject
    public EventBusProperties(
            List<CodecProperties> codecs,
            @Bindable(defaultValue = "false") boolean failOnCodecError) {
        this.codecs = codecs;
        this.failOnCodecError = failOnCodecError;
    }
}
