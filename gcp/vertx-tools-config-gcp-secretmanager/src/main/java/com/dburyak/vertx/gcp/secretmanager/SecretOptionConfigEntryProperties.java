package com.dburyak.vertx.gcp.secretmanager;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Configuration properties for each config option backed by GSM secret.
 */
@EachProperty(value = "vertx.gcp.config.secret-manager.secret-config-options", list = true)
@Getter
public class SecretOptionConfigEntryProperties {

    /**
     * Name of the config option. Required.
     */
    private final String configOption;

    /**
     * Secret name (secret id). Required.
     */
    private final String secretName;

    /**
     * GCP project ID to use for this secret and topic instead of default one. Optional. If not provided then default
     * project ID is used.
     */
    private final String projectId;

    /**
     * Pubsub topic to listen for secret updates instead of default notification topic configured globally for gsm
     * config store. Optional. If not provided and global one is not set either, then secret updates notifications are
     * disabled for this secret.
     */
    private final String notificationTopic;

    @ConfigurationInject
    public SecretOptionConfigEntryProperties(
            @NotBlank String configOption,
            @NotBlank String secretName,
            @Nullable String projectId,
            @Nullable String notificationTopic) {
        this.configOption = configOption;
        this.secretName = secretName;
        this.projectId = projectId;
        this.notificationTopic = notificationTopic;
    }
}
