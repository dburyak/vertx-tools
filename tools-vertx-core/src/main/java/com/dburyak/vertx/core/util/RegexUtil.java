package com.dburyak.vertx.core.util;

import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
@Getter
public class RegexUtil {
    private static final String UUID_PATTERN_STR =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private Pattern uuidPattern;

    public String getUuidPatternString() {
        return UUID_PATTERN_STR;
    }

    @PostConstruct
    private void init() {
        uuidPattern = Pattern.compile(UUID_PATTERN_STR);
    }
}
