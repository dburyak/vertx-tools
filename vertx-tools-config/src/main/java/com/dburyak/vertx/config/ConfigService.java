package com.dburyak.vertx.config;

import io.micronaut.context.annotation.Requires;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.config.ConfigChange;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * More friendly wrapper around {@link ConfigRetriever}.
 */
@Singleton
@Requires(missingBeans = ConfigService.class)
@Slf4j
public class ConfigService {
    private static final ConfigChange DUMMY_CHANGE = new ConfigChange(new JsonObject(), new JsonObject());

    private final ConfigRetriever cfgRetriever;

    /**
     * Broadcasted hot stream of config changes. Config changes that happened before subscription are NOT emitted.
     */
    @Getter
    private final Flowable<ConfigChange> changes;

    private volatile Disposable changesSubscription;

    public ConfigService(ConfigRetriever cfgRetriever) {
        this.cfgRetriever = cfgRetriever;
        var changesWithLastCached = Flowable.<ConfigChange>create(emitter -> cfgRetriever.listen(cfgChange -> {
                    if (emitter.isCancelled()) {
                        // CfgRetriever.listen() does not support unregistering listeners, so that's the best we can do
                        return;
                    }
                    if (log.isDebugEnabled()) {
                        var prev = cfgChange.getPreviousConfiguration();
                        var next = cfgChange.getNewConfiguration();
                        var allKeys = new HashSet<>(prev.fieldNames());
                        allKeys.addAll(next.fieldNames());
                        var updOpts = allKeys.stream()
                                .filter(key -> !Objects.equals(prev.getValue(key), next.getValue(key)))
                                .collect(toSet());
                        log.debug("config changed: updOpts={}", updOpts);
                    }
                    emitter.onNext(cfgChange);
                }), BackpressureStrategy.BUFFER)
                .replay(1, true)
                .autoConnect(1, d -> changesSubscription = d);
        // replay(1) + autoConnect + skip(1) - is the only way to have long-lived broadcast stream without any
        // buffering/caching, so that new subscribers will receive only *NEW* config changes that happened *AFTER* they
        // subscribed
        changes = changesWithLastCached.ambWith(changesWithLastCached.startWithItem(DUMMY_CHANGE)).skip(1);
    }

    /**
     * Get current config. If config is loaded and cached already, then cached value is returned. Otherwise, config is
     * loaded according to Vertx config retriever settings.
     *
     * @return current config
     */
    public Single<JsonObject> getConfig() {
        return Maybe.fromSupplier(() -> {
                    var cachedCfg = cfgRetriever.getCachedConfig();
                    return !cachedCfg.isEmpty() ? cachedCfg : null;
                })
                .switchIfEmpty(cfgRetriever.rxGetConfig());
    }

    /**
     * Get stream of configurations. Current config is always emitted first, and then updated configs are emitted on
     * config changes.
     *
     * @return stream of all configurations
     */
    public Flowable<JsonObject> getStream() {
        return changes.map(ConfigChange::getNewConfiguration)
                .startWith(getConfig());
    }

    /**
     * Get stream of configurations. Current config is always emitted first, and then updated configs are emitted only
     * if at least one of the specified keys was changed.
     *
     * @param keys config keys to watch for changes
     *
     * @return filtered stream of configurations
     */
    public Flowable<JsonObject> streamForKeys(Set<String> keys) {
        return changes.filter(cfgChange -> {
                    var prev = cfgChange.getPreviousConfiguration();
                    var next = cfgChange.getNewConfiguration();
                    return keys.stream()
                            .anyMatch(key -> !Objects.equals(prev.getValue(key), next.getValue(key)));
                })
                .map(ConfigChange::getNewConfiguration)
                .startWith(getConfig());
    }

    /**
     * Get stream of configurations. Current config is always emitted first, and then updated configs are emitted only
     * if specified key was changed.
     *
     * @param key config key to watch for changes
     *
     * @return filtered stream of configurations
     */
    public Flowable<JsonObject> getStreamForKey(String key) {
        return changes.filter(cfgChange -> {
                    var prev = cfgChange.getPreviousConfiguration();
                    var next = cfgChange.getNewConfiguration();
                    return !Objects.equals(prev.getValue(key), next.getValue(key));
                })
                .map(ConfigChange::getNewConfiguration)
                .startWith(getConfig());
    }

    /**
     * Get stream of configurations. Current config is always emitted first, and then updated configs are emitted only
     * if at least one of keys starting with given prefix was changed.
     *
     * @param prefix prefix for config keys to watch for changes
     *
     * @return filtered stream of configurations
     */
    public Flowable<JsonObject> streamForPrefix(String prefix) {
        return changes.filter(cfgChange -> {
                    var prev = cfgChange.getPreviousConfiguration();
                    var next = cfgChange.getNewConfiguration();
                    var allKeys = new HashSet<>(prev.fieldNames());
                    allKeys.addAll(next.fieldNames());
                    return allKeys.stream().anyMatch(key ->
                            key.startsWith(prefix) && !Objects.equals(prev.getValue(key), next.getValue(key)));
                })
                .map(ConfigChange::getNewConfiguration)
                .startWith(getConfig());
    }

    /**
     * Get stream of configurations. Current config is always emitted first, and then updated configs are emitted only
     * if at least one of keys starting with given prefixes was changed.
     *
     * @param prefixes prefixes for config keys to watch for changes
     *
     * @return filtered stream of configurations
     */
    public Flowable<JsonObject> streamForPrefixes(Set<String> prefixes) {
        return changes.filter(cfgChange -> {
                    var prev = cfgChange.getPreviousConfiguration();
                    var next = cfgChange.getNewConfiguration();
                    var allKeys = new HashSet<>(prev.fieldNames());
                    allKeys.addAll(next.fieldNames());
                    return allKeys.stream()
                            .filter(key -> prefixes.stream().anyMatch(key::startsWith))
                            .anyMatch(key -> !Objects.equals(prev.getValue(key), next.getValue(key)));
                })
                .map(ConfigChange::getNewConfiguration)
                .startWith(getConfig());
    }

    @PreDestroy
    public void destroy() {
        var changesSubscriptionRef = changesSubscription;
        if (changesSubscriptionRef != null) {
            changesSubscriptionRef.dispose();
        }
    }
}
