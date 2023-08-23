package com.dburyak.vertx.gcp.secretmanager;

import io.micronaut.context.ApplicationContext;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class GcpSecretManagerConfigStore implements ConfigStore {

    // just a public alias
    public static final String TYPE = GcpSecretManagerConfigStoreSpiFactory.NAME;

    private final Vertx spiVertx;
    private final JsonObject spiCfg;

    @Setter(onMethod_ = @Inject)
    private volatile ApplicationContext appCtx;

    @Setter(onMethod_ = @Inject)
    private volatile io.vertx.rxjava3.core.Vertx vertx;

    @Setter(onMethod_ = @Inject)
    private volatile GcpSecretManagerConfigProperties cfg;

    @Setter(onMethod_ = @Inject)
    private volatile GcpSecretManager secretManager;


    @Override
    public Future<Buffer> get() {
        if (cfg.getSecretConfigOptions().isEmpty()) {
            return Future.succeededFuture(Buffer.buffer("{}"));
        }
        var resultPromise = Promise.<Buffer>promise();
        var startedAt = Instant.now();
        Observable.fromIterable(cfg.getSecretConfigOptions().entrySet())
                .flatMapSingle(secretOpt -> secretManager.getSecretString(secretOpt.getValue())
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
}