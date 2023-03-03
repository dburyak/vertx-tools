package com.dburyak.vertx.eventbus.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import lombok.Data;

import java.util.List;

/**
 * Global configuration for EventBus codec.
 */
@EachProperty("vertx.event-bus.codec")
@Data
public class CodecProperties {

    /**
     * Codec name to use when registering on the EventBus. This name can be used to specify which codec to use when
     * sending messages over EventBus.
     */
    private String name;

    /**
     * Whether this codec is enabled and should be registered on the EventBus.
     */
    private boolean isEnabled;

    /**
     * Codec implementation fully-qualified class name.
     */
    private String type;

    /**
     * Whether this codec should be registered as "default". Default codecs are registered by message DTO java
     * class. So whenever object of that class is sent corresponding registered "default" codec is used, sender does
     * not have to explicitly specify which exact codec to use by specifying codec "name".
     */
    private boolean isDefault;

    /**
     * Message DTO java classes this codec should convert if it is registered as "default" codec. In other words, list
     * of java classes this codec should be used to convert when no codec name is specified in delivery options, or
     * delivery options are not specified at all.
     */
    private List<String> defaultTypes;

    public CodecProperties(@Parameter("name") String name) {
        this.name = name;
    }
}
