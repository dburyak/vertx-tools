package com.archiuse.mindis.config

import com.archiuse.mindis.json.JsonHelper
import io.micronaut.context.annotation.Property
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.config.ConfigRetriever

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Duration

import static java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Mindis app config retriever.
 * Provides types conversion and destructuring from flat form into nested groovy map.
 */
@Singleton
class AppConfig {

    @Property(name = 'mindis.config.format.decode-special')
    boolean decodeSpecial = true

    @Property(name = 'mindis.config.format.map-key-separator')
    String mapKeySeparator = '.'

    @Property(name = 'mindis.config.format.list-join-separator')
    String listJoinSeparator = ','

    @Property(name = 'mindis.config.reader.stream-min-interval')
    Duration configStreamMinInterval = Duration.ofSeconds(30)

    @Inject
    ConfigRetriever vertxConfigRetriever

    @Inject
    JsonHelper jsonHelper

    @Inject
    ConfigHelper configHelper

    private Observable<Map<String, Object>> changeStreamShared
    private Disposable changeStreamSharedSubscription

    /**
     * Get app config.
     * @return type converted app config nested map
     */
    Single<Map<String, Object>> getConfig() {
        vertxConfigRetriever.rxGetConfig()
                .map { toUnflattenConfigMap(it) }
    }

    /**
     * Get app config and subscribe to subsequent config changes.
     * @param minInterval smallest interval between two config change events, allows to throttle rapid config changes
     * @return app config stream
     */
    Observable<Map<String, Object>> getConfigStream(Duration minInterval = configStreamMinInterval) {
        changeStreamShared
                .sample(minInterval.toMillis(), MILLISECONDS, true)
                .startWith(config.toObservable())
    }

    @PostConstruct
    protected void init() {
        changeStreamShared = Observable
                .<JsonObject> create { emitter ->
                    vertxConfigRetriever.listen { cfgChange ->
                        if (!emitter.disposed) {
                            emitter.onNext(cfgChange.newConfiguration)
                        }
                    }
                }
                .map { toUnflattenConfigMap(it) }

        // we never dispose the single connection to the upstream config change source since there's no vertx API to
        // unregister the listener from configRetriever; if "refCount" was used here instead, then there would be an
        // open door for listeners resource leak in case when number of downstream subscribers dropped down to zero
        // and go up again - new subscription would be made and the previous one is never unregistered
                .publish()
                .autoConnect(1, {
                    // store single upstream subscription to be able to dispose it on bean destruction
                    changeStreamSharedSubscription = it
                })
    }

    @PreDestroy
    protected void dispose() {
        changeStreamSharedSubscription?.dispose()
    }

    private Map<String, Object> toUnflattenConfigMap(JsonObject flatJsonCfg) {
        def decodedFlatMap = jsonHelper.toMap(flatJsonCfg, decodeSpecial)
        configHelper.unflatten(decodedFlatMap, mapKeySeparator, listJoinSeparator)
    }
}
