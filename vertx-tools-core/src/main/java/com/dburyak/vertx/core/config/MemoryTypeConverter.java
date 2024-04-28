package com.dburyak.vertx.core.config;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.DefaultMutableConversionService;
import io.micronaut.core.convert.TypeConverter;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Type converter to support memory configuration properties.
 */
@Singleton
public class MemoryTypeConverter implements TypeConverter<String, Memory> {
    private static final Pattern SPACES = Pattern.compile("[\\s_]+");
    private static final Pattern MEMORY_PATTERN = Pattern.compile("^(?<num>\\d+|\\d+\\.\\d+)\\s*(?<unit>[a-zA-Z]+)?$");

    @Override
    public Optional<Memory> convert(String propValueStr, Class<Memory> targetType, ConversionContext context) {
        var memStr = SPACES.matcher(propValueStr).replaceAll("").toLowerCase();
        var matcher = MEMORY_PATTERN.matcher(memStr);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        var numStr = matcher.group("num");
        var unitStr = matcher.group("unit");
        Long longNum = null;
        Double doubleNum = null;
        try {
            longNum = Long.parseLong(numStr);
        } catch (NumberFormatException e) {
            try {
                doubleNum = Double.parseDouble(numStr);
            } catch (NumberFormatException e2) {
                return Optional.empty();
            }
        }
        if (unitStr == null) {
            return longNum != null ? Optional.of(Memory.ofBytes(longNum)) : Optional.empty();
        }
        return switch (unitStr.toLowerCase()) {
            case "b", "byte", "bytes" -> longNum != null ? Optional.of(Memory.ofBytes(longNum)) : Optional.empty();
            case "k", "kb", "kilobyte", "kilobytes" ->
                    Optional.of(longNum != null ? Memory.ofKb(longNum) : Memory.ofKb(doubleNum));
            case "m", "mb", "megabyte", "megabytes" ->
                    Optional.of(longNum != null ? Memory.ofMb(longNum) : Memory.ofMb(doubleNum));
            case "g", "gb", "gigabyte", "gigabytes" ->
                    Optional.of(longNum != null ? Memory.ofGb(longNum) : Memory.ofGb(doubleNum));
            case "t", "tb", "terabyte", "terabytes" ->
                    Optional.of(longNum != null ? Memory.ofTb(longNum) : Memory.ofTb(doubleNum));
            default -> Optional.empty();
        };
    }

    @PostConstruct
    public void registerInShared() {
        // doing this allows to use this converter in @Bindable(defaultValue = "10mb")
        ((DefaultMutableConversionService) ConversionService.SHARED).addConverter(String.class, Memory.class, this);
    }
}
