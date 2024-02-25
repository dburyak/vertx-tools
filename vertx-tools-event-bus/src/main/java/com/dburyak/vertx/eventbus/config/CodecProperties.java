package com.dburyak.vertx.eventbus.config;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.bind.annotation.Bindable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

/**
 * Global configuration for EventBus codec.
 */
@EachProperty("vertx.event-bus.codec")
@Getter
public class CodecProperties {

    /**
     * Codec name to use when registering on the EventBus. This name can be used to specify which codec to use when
     * sending messages over EventBus.
     */
    private final String name;

    /**
     * Whether this codec is enabled and should be registered on the EventBus.
     */
    private final boolean enabled;

    /**
     * Codec implementation fully-qualified class name.
     */
    private final String type;

    /**
     * Whether this codec should be registered as "default". Default codecs are registered by message DTO java class. So
     * whenever object of that class is sent corresponding registered "default" codec is used, sender does not have to
     * explicitly specify which exact codec to use by specifying codec "name".
     */
    private final boolean isDefault;

    /**
     * Message DTO java classes this codec should convert if it is registered as "default" codec. In other words, list
     * of java classes this codec should be used to convert when no codec name is specified in delivery options, or
     * delivery options are not specified at all.
     */
    private final List<String> defaultTypes;

    /**
     * Constructor.
     *
     * @param name codec {@link #name}
     */
    @ConfigurationInject
    public CodecProperties(
            @Parameter("name") String name,
            @Bindable(defaultValue = "true") boolean enabled,
            @NotBlank String type,
            @Bindable(defaultValue = "false") boolean isDefault,
            List<String> defaultTypes) {
        this.name = name;
        this.enabled = enabled;
        this.type = type;
        this.isDefault = isDefault;
        this.defaultTypes = defaultTypes;
    }
}
