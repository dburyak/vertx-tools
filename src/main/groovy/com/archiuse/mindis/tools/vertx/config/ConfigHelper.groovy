package com.archiuse.mindis.tools.vertx.config

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

    JsonObject toPlain(JsonObject cfgJson) {
        // TODO: implement
        assert false: 'not implemented'
    }

    Map<String, Object> toPlain(Map<String, Object> cfgMap) {
        // TODO: implement
        assert false: 'not implemented'
    }
}
