package com.dburyak.vertx.eventbus.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import jakarta.inject.Inject;
import lombok.Data;

import java.util.List;

/**
 * EventBus configuration.
 */
@ConfigurationProperties("vertx.event-bus")
@Context
@Data
public class EventBusProperties {

    /**
     * EventBus message codecs configurations.
     */
    private List<CodecProperties> codecs;

    /**
     * Whether to fail application startup process if error happened while loading/registering codec. Setting this
     * option to true allows to fail fast if any codec is misconfigured. Otherwise, such erroneous codec will be
     * skipped.
     */
    private boolean failOnCodecError = false;

    /**
     * Set EventBus message codecs configurations.
     *
     * @param codecs codecs
     */
    @Inject
    public void setCodecs(List<CodecProperties> codecs) {
        this.codecs = codecs;
    }

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
}
