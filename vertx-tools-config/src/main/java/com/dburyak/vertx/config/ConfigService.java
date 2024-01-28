package com.dburyak.vertx.config;

import io.micronaut.context.annotation.Requires;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
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

@Singleton
@Requires(missingBeans = ConfigService.class)
@Slf4j
public class ConfigService {
    private final ConfigRetriever cfgRetriever;

    @Getter
    private final Flowable<ConfigChange> changes;

    private volatile boolean closed = false;

    public ConfigService(ConfigRetriever cfgRetriever) {
        this.cfgRetriever = cfgRetriever;
        changes = Flowable.<ConfigChange>create(emitter -> cfgRetriever.listen(cfgChange -> {
                    if (closed) {
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
                }), BackpressureStrategy.DROP)
                .publish(1).autoConnect();
    }

    public Single<JsonObject> getConfig() {
        return cfgRetriever.rxGetConfig();
    }

    public Flowable<JsonObject> getStream() {
        return changes.map(ConfigChange::getNewConfiguration)
                .startWith(cfgRetriever.rxGetConfig());
    }

    public Flowable<JsonObject> getStream(Set<String> keys) {
        return changes.filter(cfgChange -> {
                    var prev = cfgChange.getPreviousConfiguration();
                    var next = cfgChange.getNewConfiguration();
                    return keys.stream()
                            .anyMatch(key -> !Objects.equals(prev.getValue(key), next.getValue(key)));
                })
                .map(ConfigChange::getNewConfiguration)
                .startWith(cfgRetriever.rxGetConfig());
    }

    public Flowable<JsonObject> getStreamOfPrefixes(Set<String> prefixes) {
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
                .startWith(cfgRetriever.rxGetConfig());
    }

    @PreDestroy
    public void destroy() {
        closed = true;
    }
}
