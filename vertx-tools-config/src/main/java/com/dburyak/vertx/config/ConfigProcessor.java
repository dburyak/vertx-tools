package com.dburyak.vertx.config;

import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * Config processor is a function that takes a config object and returns a modified config object. It can return both
 * the same object or a new object.
 */
public interface ConfigProcessor extends Function<JsonObject, JsonObject> {
}
