package com.dburyak.vertx.gcp.secretmanager;

import io.micronaut.context.annotation.EachProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Configuration properties for each config option backed by GSM secret.
 */
@EachProperty(value = "vertx.gcp.config.secret-manager.secret-config-options", list = true)
@Data
public class SecretOptionConfigEntryProperties {

    /**
     * Name of the config option. Required.
     */
    @NotBlank
    private String configOption;

    /**
     * Secret name (secret id). Required.
     */
    @NotBlank
    private String secretName;

    /**
     * GCP project ID to use for this secret and topic instead of default one. Optional. If null or empty then default
     * project ID is used.
     */
    private String projectId;

    /**
     * Pubsub topic to listen for secret updates instead of default notification topic configured globally for gsm
     * config store. Optional. If this option is null or empty and global one is null or empty too, then secret updates
     * notifications are disabled for this secret.
     */
    private String notificationTopic;
}
