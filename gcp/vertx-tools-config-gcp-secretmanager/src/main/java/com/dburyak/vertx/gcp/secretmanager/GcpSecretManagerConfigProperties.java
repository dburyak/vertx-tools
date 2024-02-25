package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for vertx config store backed by GCP Secret Manager.
 */
@ConfigurationProperties("vertx.gcp.config.secret-manager")
@Getter
public class GcpSecretManagerConfigProperties {

    /**
     * Whether to enable GCP Secret Manager config store.
     */
    private final boolean enabled;

    /**
     * Whether to fail application startup process if error happened while loading config from GCP Secret Manager.
     */
    private final boolean optional;

    /**
     * GCP project ID to use by default. Optional. If not specified, then default project ID is used.
     */
    private final String projectId;

    /**
     * Whether to enable periodic refresh (re-read) of secrets from Google Secret Manager.
     */
    private final boolean refreshEnabled;

    /**
     * How often to re-read secrets from Google Secret Manager if periodic refresh is enabled. Zero or negative value
     * means no refresh.
     */
    private final Duration refreshPeriod;

    /**
     * Whether to enable listening of secret updates events over pubsub, and refresh secrets accordingly.
     */
    private final boolean pubsubNotificationsEnabled;

    /**
     * Pubsub topic to listen for secret updates. Optional. If not specified then secret updates notifications are
     * enabled only for secrets with individual per-option notification topics configured.
     */
    private final String pubsubNotificationTopic;

    /**
     * Individual secret config options.
     */
    private final List<SecretOptionConfigEntryProperties> secretConfigOptions;

    @ConfigurationInject
    public GcpSecretManagerConfigProperties(
            @Bindable(defaultValue = "true") @Nullable boolean enabled,
            @Bindable(defaultValue = "false") boolean optional,
            @Nullable String projectId,
            @Bindable(defaultValue = "false") boolean refreshEnabled,
            @Bindable(defaultValue = "24h") @MinDuration("1s") @NotNull Duration refreshPeriod,
            @Bindable(defaultValue = "false") boolean pubsubNotificationsEnabled,
            @Nullable String pubsubNotificationTopic,
            List<SecretOptionConfigEntryProperties> secretConfigOptions) {
        this.enabled = enabled;
        this.optional = optional;
        this.projectId = projectId;
        this.refreshEnabled = refreshEnabled;
        this.refreshPeriod = refreshPeriod;
        this.pubsubNotificationsEnabled = pubsubNotificationsEnabled;
        this.pubsubNotificationTopic = pubsubNotificationTopic;
        this.secretConfigOptions = secretConfigOptions;
    }
}
