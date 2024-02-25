package com.dburyak.vertx.config;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import lombok.Getter;

import java.time.Duration;

/**
 * Configuration properties for {@link io.vertx.config.ConfigRetriever}.
 */
@ConfigurationProperties("vertx.config")
@Getter
public class ConfigRetrieverProperties {

    /**
     * Whether to include default property stores:
     * <ul>
     *     <li>verticle config</li>
     *     <li>system properties</li>
     *     <li>environment variables</li>
     *     <li>config file, {@code conf/config.json} by default, can be overridden using the
     *     {@code vertx-config-path} system property or {@code VERTX_CONFIG_PATH} environment variable</li>
     * See <a href="https://vertx.io/docs/vertx-config/java/#_using_the_config_retriever">vertx-config docs</a>
     * for more information.
     */
    private final boolean includeDefaultStores;

    /**
     * Period of scanning for changes in the configuration. Using vertx defaults (5s) when not specified explicitly.
     */
    private final Duration scanPeriod;

    /**
     * Whether to process {@code ${}} placeholders in the configuration.
     */
    private final boolean processPlaceholders;

    @ConfigurationInject
    public ConfigRetrieverProperties(
            @Bindable(defaultValue = "true") boolean includeDefaultStores,
            @Nullable @MinDuration("1s") Duration scanPeriod,
            @Bindable(defaultValue = "true") boolean processPlaceholders) {
        this.includeDefaultStores = includeDefaultStores;
        this.scanPeriod = scanPeriod;
        this.processPlaceholders = processPlaceholders;
    }
}
