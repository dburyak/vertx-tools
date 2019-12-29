package com.archiuse.mindis.config

import com.archiuse.mindis.json.JsonHelper
import io.reactivex.Scheduler
import io.reactivex.Single
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.config.ConfigRetriever
import io.vertx.reactivex.core.RxHelper
import io.vertx.reactivex.core.Vertx
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.time.Duration

import static java.util.concurrent.TimeUnit.SECONDS

class AppConfigSpec extends Specification {
    AppConfig appConfig = new AppConfig()

    static k7Duration = Duration.ofSeconds(3)
    static k7DurationStr = k7Duration.toString()
    static cfgJsonMap = [
            k1     : 'v1',
            k2     : "3.14",
            k3     : "1,2,3",
            'k5.k6': 'v6',
            'k5.k7': k7DurationStr
    ]
    static cfgJson = new JsonObject(cfgJsonMap)
    static cfgDecodedMap = [
            k1     : 'v1',
            k2     : 3.14,
            k3     : [1, 2, 3],
            'k5.k6': 'v6',
            'k5.k7': k7Duration
    ]
    static cfgUnflattenMap = [
            k1: 'v1',
            k2: 3.14,
            k3: [1, 2, 3],
            k5: [
                    k6: 'v6',
                    k7: k7Duration
            ]
    ]

    @Shared
    Vertx vertx = Vertx.vertx()

    @Shared
    Scheduler vertxRxScheduler = RxHelper.scheduler(vertx)

    void setup() {
        appConfig.vertxConfigRetriever = Mock(ConfigRetriever)
        appConfig.jsonHelper = Mock(JsonHelper)
        appConfig.configHelper = Mock(ConfigHelper)
    }

    def 'getConfig delegates correctly'() {
        when:
        def cfg = appConfig.getConfig().blockingGet()

        then:
        noExceptionThrown()
        1 * appConfig.vertxConfigRetriever.rxGetConfig() >> Single.just(cfgJson)
        1 * appConfig.jsonHelper.fromJson(cfgJson, appConfig.decodeSpecial) >> cfgDecodedMap
        1 * appConfig.configHelper.unflatten(cfgDecodedMap, appConfig.mapKeySeparator,
                appConfig.listJoinSeparator) >> cfgUnflattenMap
        cfg == cfgUnflattenMap
    }

    @Timeout(value = 5, unit = SECONDS)
    def 'getPeriodicStream delegates correctly'() {
        given:
        appConfig = Spy(appConfig)
        def num = 3
        appConfig.vertxRxScheduler = vertxRxScheduler

        when:
        def cfg = appConfig.getPeriodicStream(Duration.ZERO, Duration.ofMillis(1))
                .take(num)
                .test()
                .await()

        then:
        noExceptionThrown()
        num * appConfig.getConfig() >> Single.just(cfgUnflattenMap)
        cfg.assertValueSequence([cfgUnflattenMap] * num)
    }
}
