package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Configuration properties for vertx config store backed by GCP Secret Manager.
 */
@ConfigurationProperties("vertx.gcp.config.secret-manager")
@Data
public class GcpSecretManagerConfigProperties {

    /**
     * Whether to enable GCP Secret Manager config store.
     */
    private boolean enabled = true;

    /**
     * Whether to fail application startup process if error happened while loading config from GCP Secret Manager.
     */
    private boolean optional = false;

    /**
     * GCP project ID to use by default. Optional. If null or empty then default project ID is used.
     */
    private String projectId;

    /**
     * Whether to enable periodic refresh (re-read) of secrets from Google Secret Manager.
     */
    private boolean refreshEnabled = false;

    /**
     * How often to re-read secrets from Google Secret Manager if periodic refresh is enabled. Zero or negative value
     * means no refresh.
     */
    @MinDuration("1s")
    @NotNull
    private Duration refreshPeriod = Duration.ofHours(24);

    /**
     * Whether to enable listening of secret updates events over pubsub, and refresh secrets accordingly.
     */
    private boolean pubsubNotificationsEnabled = false;

    /**
     * Individual secrets config options.
     */
    @NotNull
    private List<SecretConfigEntry> secretConfigOptions = emptyList();

    @Data
    public class SecretConfigEntry {

        /**
         * Name of the config option. Required.
         */
        private String configOption;

        /**
         * Secret name (secret id). Required.
         */
        private String secretName;

        /**
         * GCP project ID to use for this secret instead of default one. Optional. If null or empty then default project
         * ID is used.
         */
        private String projectId;

        /**
         * Pubsub subscription to listen for secret updates. Optional. If null or empty then secret updates
         * notifications are disabled for this secret.
         */
        private String notificationSubscription;
    }
}
