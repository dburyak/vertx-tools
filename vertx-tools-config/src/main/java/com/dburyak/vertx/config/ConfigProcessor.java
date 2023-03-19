package com.dburyak.vertx.config;

import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public interface ConfigProcessor extends Function<JsonObject, JsonObject> {
}
