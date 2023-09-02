package com.dburyak.vertx.config;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.config.impl.ConfigRetrieverImpl;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Factory for {@link ConfigRetriever} bean.
 */
@Factory
@Slf4j
public class ConfigRetrieverFactory {

    /**
     * Default {@link ConfigRetriever} bean.
     *
     * @param vertx vertx instance
     * @param configRetrieverOptions config retriever options
     * @param cfgProcessors config processors
     * @param appCtx micronaut application context
     *
     * @return config retriever bean
     */
    @Singleton
    @Requires(missingBeans = ConfigRetriever.class)
    public ConfigRetriever configRetriever(Vertx vertx, ConfigRetrieverOptions configRetrieverOptions,
            List<ConfigProcessor> cfgProcessors, ApplicationContext appCtx) {
        var cfgRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
        cfgRetriever.setConfigurationProcessor(cfg -> {
            var modCfg = cfg;
            for (var cfgProcessor : cfgProcessors) {
                modCfg = cfgProcessor.apply(modCfg);
            }
            return modCfg;
        });
        // hacky way to perform DI for instances created via SPI java mechanism
        ((ConfigRetrieverImpl) cfgRetriever.getDelegate()).getProviders().forEach(provider -> {
            appCtx.inject(provider.getStore());
            log.debug("config store registered: {}", provider.getStore().getClass());
        });
        return cfgRetriever;
    }

    /**
     * Default {@link ConfigRetrieverOptions} bean.
     *
     * @param cfg config retriever properties
     * @param cfgStores config stores
     *
     * @return config retriever options bean
     */
    @Singleton
    @Requires(missingBeans = ConfigRetrieverOptions.class)
    public ConfigRetrieverOptions configRetrieverOptions(ConfigRetrieverProperties cfg,
            List<ConfigStoreOptions> cfgStores) {
        var opts = new ConfigRetrieverOptions();
        opts.setIncludeDefaultStores(cfg.isIncludeDefaultStores());
        if (cfg.getScanPeriod() != null) {
            opts.setScanPeriod(cfg.getScanPeriod().toMillis());
        }
        cfgStores.forEach(opts::addStore);
        return opts;
    }
}
