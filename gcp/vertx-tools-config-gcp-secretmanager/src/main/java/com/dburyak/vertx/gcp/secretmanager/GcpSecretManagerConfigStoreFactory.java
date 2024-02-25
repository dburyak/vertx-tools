package com.dburyak.vertx.gcp.secretmanager;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.config.ConfigStoreOptions;

import static com.dburyak.vertx.gcp.secretmanager.GcpSecretManagerConfigStore.TYPE;

/**
 * Factory for {@link ConfigStoreOptions} for GCP Secret Manager config store.
 */
@Factory
public class GcpSecretManagerConfigStoreFactory {

    /**
     * Creates config store options for GCP Secret Manager config store.
     *
     * @param cfg GSM config properties
     *
     * @return GSM config store options
     */
    @Bean
    @Requires(bean = GcpSecretManagerConfigProperties.class, beanProperty = "enabled", value = "true",
            defaultValue = "true")
    public ConfigStoreOptions gcpSecretManagerConfigStore(GcpSecretManagerConfigProperties cfg) {
        return new ConfigStoreOptions()
                .setType(TYPE)
                .setOptional(cfg.isOptional());
    }
}
