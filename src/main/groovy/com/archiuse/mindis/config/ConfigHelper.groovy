package com.archiuse.mindis.config

import io.vertx.core.json.JsonObject

class ConfigHelper {
    private static final DEFAULT_MAP_KEY_SEPARATOR = '.'
    private static final DEFAULT_LIST_JOIN_SEPARATOR = ','

    static String getDefaultMapKeySeparator() {
        DEFAULT_MAP_KEY_SEPARATOR
    }

    static String getDefaultListJoinSeparator() {
        DEFAULT_LIST_JOIN_SEPARATOR
    }

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

    JsonObject flatten(JsonObject cfgJson, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        def flatMap = flattenWithPrefix(cfgJson.map, '', keySep, listSep)
        new JsonObject(flatMap)
    }

    Map<String, Object> flatten(Map<String, Object> cfgMap, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        flattenWithPrefix(cfgMap, '', keySep, listSep) as Map<String, Object>
    }

    JsonObject flattenToJson(Map<String, Object> cfgMap, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        new JsonObject(flatten(cfgMap, keySep, listSep))
    }

    Map<String, Object> flattenToMap(JsonObject cfgJson, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        flatten(cfgJson.map, keySep, listSep)
    }

    private def flattenWithPrefix(Map value, String prefix, String mapSep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        value.inject([:]) { flatMap, e ->
            def nestedPrefix = prefix ? prefix + mapSep + e.key : e.key as String
            flatMap << flattenWithPrefix(e.value, nestedPrefix, mapSep, listSep)
        }
    }

    private def flattenWithPrefix(Object value, String prefix, String mapSep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        [(prefix): value as String]
    }

    private def flattenWithPrefix(Iterable value, String prefix, String mapSep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        value.eachWithIndex { it, idx -> // scan for nested complex objects
            if (it instanceof Iterable || it instanceof Map) {
                throw new ComplexListConfigException(list: value, complexElement: it, complexElementIndex: idx)
            }
        }
        [(prefix): value.join(listSep)]
    }
}
