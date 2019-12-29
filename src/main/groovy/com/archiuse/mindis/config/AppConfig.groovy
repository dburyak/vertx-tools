package com.archiuse.mindis.config

import com.archiuse.mindis.json.JsonHelper
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.config.ConfigRetriever

import javax.annotation.PostConstruct
import java.time.Duration

import static java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Mindis app config retriever.
 * Provides types conversion and destructuring from flat form into nested groovy map.
 */
class AppConfig {
    boolean decodeSpecial = true
    String mapKeySeparator = '.'
    String listJoinSeparator = ','
    Duration configPeriodicStreamPeriod = Duration.ofMinutes(1)
    Duration configPeriodicStreamInitialDelay = Duration.ZERO
    Duration configChangeStreamMinInterval = Duration.ofMinutes(1)

    ConfigRetriever vertxConfigRetriever
    JsonHelper jsonHelper
    ConfigHelper configHelper
    Scheduler vertxRxScheduler

    private Observable<Map<String, Object>> changeStreamShared

    /**
     * Get app config.
     * @return app config map
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
    Observable<Map<String, Object>> getChangeStream(Duration minInterval = configChangeStreamMinInterval) {
        changeStreamShared
                .sample(minInterval.toMillis(), MILLISECONDS, true)
                .startWith(config.toObservable())
    }

    /**
     * Get app config periodic stream
     * @param initialDelay initial delay before retrieving config
     * @param period period of config retrieval
     * @return config stream
     */
    Observable<Map<String, Object>> getPeriodicStream(Duration initialDelay = configPeriodicStreamInitialDelay,
            Duration period = configPeriodicStreamPeriod) {
        def initialMs = initialDelay.toMillis()
        def periodMs = period.toMillis()
        Observable.interval(initialMs, periodMs, MILLISECONDS, vertxRxScheduler)
                .flatMapSingle { config }
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
                .share()
    }

    private Map<String, Object> toUnflattenConfigMap(JsonObject flatJsonCfg) {
        def decodedFlatMap = jsonHelper.fromJson(flatJsonCfg, decodeSpecial)
        configHelper.unflatten(decodedFlatMap, mapKeySeparator, listJoinSeparator)
    }
}
