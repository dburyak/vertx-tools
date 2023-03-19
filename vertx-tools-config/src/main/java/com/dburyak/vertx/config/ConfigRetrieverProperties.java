package com.dburyak.vertx.config;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import lombok.Data;

import java.time.Duration;

@ConfigurationProperties("vertx.config")
@Context
@Data
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
    private boolean includeDefaultStores = true;

    /**
     * Period of scanning for changes in the configuration.
     * Using vertx defaults (5s) when not specified explicitly.
     */
    @MinDuration("1s")
    private Duration scanPeriod;
}
