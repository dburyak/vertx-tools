package com.archiuse.mindis.config

import com.archiuse.mindis.json.JsonHelper
import groovy.transform.TupleConstructor
import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.reactivex.RxHelper
import io.vertx.reactivex.config.ConfigRetriever

import java.time.Duration

import static java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Mindis app config retriever. Provides types conversion.
 */
@TupleConstructor
class AppConfigRetriever {
    ConfigRetriever configRetriever
    boolean encodeSpecial = true
    Duration configStreamPeriod = Duration.ofMinutes(1)
    Duration configStreamInitialDelay = Duration.ZERO

    Vertx vertx
    Context vertxCtx
    JsonHelper jsonHelper
    ConfigHelper configHelper

    Single<Map<String, Object>> getAppConfig() {
        configRetriever.rxGetConfig()
                .map { json ->  }
    }

    Observable<Map<String, Object>> appConfigStream(Duration initialDelay = configStreamInitialDelay,
            Duration period = configStreamPeriod) {
        def initialMs = initialDelay.toMillis()
        def periodMs = period.toMillis()
        Observable.interval(initialMs, periodMs, MILLISECONDS, RxHelper.scheduler(vertxCtx ?: vertx))
                .flatMapSingle { appConfig }
    }
}
