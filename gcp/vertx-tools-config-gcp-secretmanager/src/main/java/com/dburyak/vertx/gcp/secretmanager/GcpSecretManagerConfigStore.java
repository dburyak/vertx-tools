package com.dburyak.vertx.gcp.secretmanager;

import io.reactivex.rxjava3.core.Observable;
import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Config store implementation for GCP Secret Manager.
 * <p>
 * This class is instantiated via SPI mechanism (from {@link GcpSecretManagerConfigStoreSpiFactory}) outside of DI
 * container, by direct constructor call. Therefore, all the collaborator beans are injected via setters after
 * instantiation by DI container explicit injection.
 */
@Singleton
@Slf4j
public class GcpSecretManagerConfigStore implements ConfigStore {

    /**
     * Config store type for registering in vertx.
     */
    public static final String TYPE = GcpSecretManagerConfigStoreSpiFactory.NAME;

    private volatile GcpSecretManagerConfigProperties cfg;
    private volatile GcpSecretManager secretManager;


    @Override
    public Future<Buffer> get() {
        var cfgLocal = cfg;
        if (cfgLocal.getSecretConfigOptions().isEmpty()) {
            return Future.succeededFuture(Buffer.buffer("{}"));
        }
        var gsmLocal = secretManager;
        var resultPromise = Promise.<Buffer>promise();
        var startedAt = Instant.now();
        Observable.fromIterable(cfgLocal.getSecretConfigOptions().entrySet())
                .flatMapSingle(secretOpt -> gsmLocal.getSecretString(secretOpt.getValue())
                        .map(secretValue -> Map.entry(secretOpt.getKey(), secretValue)))
                .toMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnSuccess(m -> log.debug("gsm secret options retrieved: numSecrets={}, duration={}",
                        m.size(), Duration.between(startedAt, Instant.now())))
                .map(m -> {
                    var json = new JsonObject();
                    m.forEach(json::put);
                    return json;
                })
                .subscribe(json -> {
                    resultPromise.complete(Buffer.buffer(json.encode()));
                }, err -> {
                    log.error("gsm secrets retrieval failed: err={}", err.toString(), err);
                    resultPromise.fail(err);
                });
        return resultPromise.future();
    }

    @Override
    public Future<Void> close() {
        // nothing to close here, this object is aggregate - collaborators are closed elsewhere
        return Future.succeededFuture();
    }

    /**
     * Set secret manager.
     *
     * @param secretManager secret manager
     */
    @Inject
    public void setSecretManager(GcpSecretManager secretManager) {
        this.secretManager = secretManager;
    }

    /**
     * Set GSM config.
     *
     * @param cfg GSM config
     */
    @Inject
    public void setCfg(GcpSecretManagerConfigProperties cfg) {
        this.cfg = cfg;
    }
}
