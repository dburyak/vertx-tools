package com.dburyak.vertx.eventbus.kryo.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import javax.validation.constraints.Positive;

/**
 * Kryo event bus codec configuration.
 */
@ConfigurationProperties("vertx.event-bus.codec.kryo")
@Data
public class KryoCodecProperties {

    /**
     * Initial (minimal) size of kryo output buffer. Outputs are created per vertx thread. E.g. in vertx with 16
     * event-loop threads there will be 16 outputs with buffers; if each buffer has initial size of 4k then there will
     * be 4k*16=64k memory minimum allocated for kryo outputs.
     */
    @Positive
    private int outputBufferInitialSize = 1024;
}
