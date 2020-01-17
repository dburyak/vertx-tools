package com.archiuse.mindis.config

import com.archiuse.mindis.VertxRxJavaSpec
import com.archiuse.mindis.json.JsonHelper
import groovy.util.logging.Slf4j
import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.config.ConfigChange
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.config.ConfigRetriever
import spock.lang.Timeout

import java.time.Duration

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 3, unit = SECONDS)
@Slf4j
class AppConfigSpec extends VertxRxJavaSpec {
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

    void setup() {
        appConfig.vertxConfigRetriever = Mock(ConfigRetriever)
        appConfig.jsonHelper = Mock(JsonHelper)
        appConfig.configHelper = Mock(ConfigHelper)
    }

    def 'getConfig delegates correctly'() {
        when:
        def cfg = appConfig.getConfig().test().await()

        then:
        noExceptionThrown()
        1 * appConfig.vertxConfigRetriever.rxGetConfig() >> Single.just(cfgJson)
        1 * appConfig.jsonHelper.toMap(cfgJson, appConfig.decodeSpecial) >> cfgDecodedMap
        1 * appConfig.configHelper.unflatten(cfgDecodedMap, appConfig.mapKeySeparator,
                appConfig.listJoinSeparator) >> cfgUnflattenMap
        cfg.assertValue(cfgUnflattenMap)
    }

    def 'getConfigStream delegates correctly'() {
        given:
        appConfig = Spy(appConfig)
        def numChanges = 3
        def cfgsJson = (1..numChanges).collect { new JsonObject(k: it) }
        def cfgsMap = (1..numChanges).collect { [k: it] }
        Handler<ConfigChange> configChangeEmitter = null

        when: 'subscribe to config stream'
        appConfig.init()
        def cfg = appConfig.getConfigStream(Duration.ofMillis(1))
                .take(numChanges + 1)
                .test()

        and: 'emit change events, not too rapidly to not run into throttling'
        Observable.interval(10, MILLISECONDS)
                .take(numChanges)
                .subscribe { configChangeEmitter(new ConfigChange(new JsonObject(), cfgsJson[it])) }

        and: 'wait for numChanges to be received'
        cfg = cfg.await()

        then:
        noExceptionThrown()

        and: 'first config is retrieved eagerly by explicit call to getConfig()'
        1 * appConfig.getConfig() >> Single.just(cfgUnflattenMap)

        and: 'single change source subscription is active while appConfig is not disposed'
        appConfig.changeStreamSharedSubscription != null
        !appConfig.changeStreamSharedSubscription.disposed

        and: 'config change listener is registered'
        1 * appConfig.vertxConfigRetriever.listen(_) >> { configChangeEmitter = it[0] }

        and: 'each cfg change is converted and unflattened'
        cfgsJson.eachWithIndex { json, idx ->
            1 * appConfig.jsonHelper.toMap(json, appConfig.decodeSpecial) >> cfgsMap[idx]
            1 * appConfig.configHelper.unflatten(cfgsMap[idx], appConfig.mapKeySeparator,
                    appConfig.listJoinSeparator) >> cfgsMap[idx]
        }

        and: 'cfg stream results are: first eager cfg followed by subsequent change events'
        cfg.assertValueSequence([cfgUnflattenMap] + cfgsMap)


        when:
        appConfig.dispose()

        then:
        appConfig.changeStreamSharedSubscription.disposed
    }
}
