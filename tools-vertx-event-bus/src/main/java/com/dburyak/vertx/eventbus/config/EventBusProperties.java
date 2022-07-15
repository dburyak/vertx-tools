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

    @Inject
    public void setCodecs(List<CodecProperties> codecs) {
        this.codecs = codecs;
    }
}
