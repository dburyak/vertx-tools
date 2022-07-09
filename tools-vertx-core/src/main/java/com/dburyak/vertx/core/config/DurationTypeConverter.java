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
            durationStr = "pt" + durationStr;
        }
        return Optional.of(Duration.parse(durationStr));
    }
}
