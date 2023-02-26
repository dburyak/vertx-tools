package com.dburyak.vertx.core.util;

import jakarta.inject.Singleton;

import java.util.regex.Pattern;

@Singleton
public class RegexUtil {
    private static final String UUID_PATTERN_STR =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_PATTERN_STR);

    public String getUuidPatternString() {
        return UUID_PATTERN_STR;
    }

    public Pattern getUuidPattern() {
        return UUID_PATTERN;
    }
}
