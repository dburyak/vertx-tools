package com.dburyak.vertx.gcp.secretmanager;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GcpSecretManagerConfigStoreSpiFactory implements ConfigStoreFactory {
    static final String NAME = "gcp-secret-manager";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ConfigStore create(Vertx vertx, JsonObject configuration) {
        log.debug("create() called with configuration: {}", configuration);
        return new GcpSecretManagerConfigStore(vertx, configuration);
    }
}
