package com.dburyak.vertx.config;

import io.micronaut.context.annotation.Requires;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Singleton;

import static java.util.Objects.requireNonNullElse;

/**
 * Config processor that replaces {@code ${}} placeholders in the configuration.
 */
@Singleton
@Requires(property = "vertx.config.process-placeholders", value = "true", defaultValue = "true")
public class ReferencePlaceholderProcessor implements ConfigProcessor {

    /**
     * Replace {@code ${}} placeholders in the configuration.
     *
     * @param config config object to process
     *
     * @return processed config object
     */
    @Override
    public JsonObject apply(JsonObject config) {
        var processedConfig = new JsonObject();
        config.fieldNames().forEach(propName -> {
            var value = config.getValue(propName);
            if (value instanceof String valueStr && (valueStr.startsWith("${") && valueStr.endsWith("}"))) {
                var refKey = valueStr.substring(2, valueStr.length() - 1);
                var replacement = config.getValue(refKey);
                processedConfig.put(propName, requireNonNullElse(replacement, value));
            } else {
                processedConfig.put(propName, value);
            }
        });
        return processedConfig;
    }
}
