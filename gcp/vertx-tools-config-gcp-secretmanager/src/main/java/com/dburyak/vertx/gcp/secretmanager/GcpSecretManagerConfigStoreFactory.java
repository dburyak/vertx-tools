package com.dburyak.vertx.gcp.secretmanager;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.config.ConfigStoreOptions;

import static com.dburyak.vertx.gcp.secretmanager.GcpSecretManagerConfigStore.TYPE;

@Factory
public class GcpSecretManagerConfigStoreFactory {

    @Bean
    @Requires(property = "vertx.gcp.config.secret-manager.enabled", value = "true", defaultValue = "true")
    public ConfigStoreOptions gcpSecretManagerConfigStore(GcpSecretManagerConfigProperties cfg) {
        return new ConfigStoreOptions()
                .setType(TYPE)
                .setOptional(cfg.isOptional());
    }
}
