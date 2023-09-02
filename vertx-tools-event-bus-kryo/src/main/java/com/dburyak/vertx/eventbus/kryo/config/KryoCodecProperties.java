package com.dburyak.vertx.eventbus.kryo.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import jakarta.validation.constraints.Positive;
import lombok.Data;

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

    /**
     * Maximum size of kryo output buffer. Value {@code -1} means no limit, which is default. Outputs are created per
     * vertx thread. E.g. in vertx with 16 event-loop threads there will be 16 outputs with buffers; if each buffer has
     * max size of 4k then there will be 4k*16=64k memory maximum allocated for kryo outputs.
     */
    private int outputBufferMaxSize = -1;
}
