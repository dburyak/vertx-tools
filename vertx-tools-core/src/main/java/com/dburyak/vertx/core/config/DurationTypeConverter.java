package com.dburyak.vertx.core.config;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.util.Optional;

@Singleton
public class DurationTypeConverter implements TypeConverter<String, Duration> {

    @Override
    public Optional<Duration> convert(String propValueStr, Class<Duration> targetType, ConversionContext context) {
        var durationStr = propValueStr.strip().toLowerCase();
        if (!durationStr.startsWith("pt")) {
            if (durationStr.endsWith("ns")) {
                var nanos = longValueWithoutSuffix(durationStr, "ns");
                return Optional.of(Duration.ofNanos(nanos));
            } else if (durationStr.endsWith("ms")) {
                var millis = longValueWithoutSuffix(durationStr, "ms");
                return Optional.of(Duration.ofMillis(millis));
            } else if (durationStr.endsWith("d")) {
                var days = longValueWithoutSuffix(durationStr, "d");
                return Optional.of(Duration.ofDays(days));
            } else {
                return Optional.of(Duration.parse("PT" + durationStr));
            }
        } else {
            return Optional.of(Duration.parse(durationStr));
        }
    }

    private Long longValueWithoutSuffix(String durationStr, String suffix) {
        return Long.parseLong(durationStr.substring(0, durationStr.length() - suffix.length()));
    }
}
