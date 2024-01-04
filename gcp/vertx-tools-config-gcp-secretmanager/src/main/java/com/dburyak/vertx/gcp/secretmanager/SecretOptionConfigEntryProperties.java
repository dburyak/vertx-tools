package com.dburyak.vertx.gcp.secretmanager;

import io.micronaut.context.annotation.EachProperty;
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
    private String configOption;

    /**
     * Secret name (secret id). Required.
     */
    private String secretName;

    /**
     * GCP project ID to use for this secret instead of default one. Optional. If null or empty then default project ID
     * is used.
     */
    private String projectId;

    /**
     * Pubsub subscription to listen for secret updates. Optional. If null or empty then secret updates notifications
     * are disabled for this secret.
     */
    private String notificationSubscription;
}
