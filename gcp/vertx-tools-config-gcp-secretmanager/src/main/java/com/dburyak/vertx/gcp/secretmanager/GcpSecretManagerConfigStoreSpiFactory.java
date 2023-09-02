package com.dburyak.vertx.gcp.secretmanager;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * SPI factory for {@link GcpSecretManagerConfigStore}. This factory is instantiated by vertx via SPI mechanism, by
 * calling default constructor.
 */
@Slf4j
public class GcpSecretManagerConfigStoreSpiFactory implements ConfigStoreFactory {
    static final String NAME = "gcp-secret-manager";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ConfigStore create(Vertx vertx, JsonObject configuration) {
        return new GcpSecretManagerConfigStore();
    }
}
