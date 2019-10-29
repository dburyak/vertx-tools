package com.archiuse.mindis.tools.vertx

import io.vertx.core.json.JsonObject

class ConfigHelper {
    Map<String, Object> findAllWithPrefix(String prefix, JsonObject cfgJson, boolean stripPrefix = false) {
        if (stripPrefix) {
            findAllWithPrefix(prefix, cfgJson, false)
                    .collectEntries { k, v -> [k - prefix, v] } as Map<String, Object>
        } else {
            cfgJson.map.findAll { it.key.startsWith prefix }
        }
    }

    Map<String, Object> findAllWithPrefixAndStrip(String prefix, JsonObject cfgJson) {
        findAllWithPrefix prefix, cfgJson, true
    }
}
