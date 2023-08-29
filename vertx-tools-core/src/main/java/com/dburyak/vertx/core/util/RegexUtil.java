package com.dburyak.vertx.core.util;

import jakarta.inject.Singleton;

import java.util.regex.Pattern;

/**
 * Utility class for widely used regex related operations.
 */
@Singleton
public class RegexUtil {
    private static final String UUID_PATTERN_STR =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_PATTERN_STR);

    /**
     * Regex pattern string for UUID.
     *
     * @return regex pattern string for UUID
     */
    public String getUuidPatternString() {
        return UUID_PATTERN_STR;
    }

    /**
     * Regex pattern for UUID.
     *
     * @return regex pattern for UUID
     */
    public Pattern getUuidPattern() {
        return UUID_PATTERN;
    }
}
