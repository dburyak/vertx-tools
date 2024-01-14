package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationProperties;
import jakarta.inject.Inject;
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
     * Pubsub topic to listen for secret updates. Optional. If null or empty then secret updates notifications are
     * enabled only for secrets with individual per-option notification topics configured.
     */
    private String pubsubNotificationTopic;

    /**
     * Individual secret config options.
     */
    private List<SecretOptionConfigEntryProperties> secretConfigOptions = emptyList();

    /**
     * Set individual secret config options.
     *
     * @param secretConfigOptions secret config options configurations
     */
    @Inject
    public void setSecretConfigOptions(@NotNull List<SecretOptionConfigEntryProperties> secretConfigOptions) {
        this.secretConfigOptions = secretConfigOptions;
    }
}
