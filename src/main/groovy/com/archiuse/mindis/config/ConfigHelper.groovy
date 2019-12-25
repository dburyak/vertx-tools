package com.archiuse.mindis.config

import com.archiuse.mindis.util.MapHelper
import io.vertx.core.json.JsonObject

class ConfigHelper {
    private static final DEFAULT_MAP_KEY_SEPARATOR = '.'
    private static final DEFAULT_LIST_JOIN_SEPARATOR = ','

    MapHelper mapHelper

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
        def flatMap = flattenRecursively(cfgJson.map, '', keySep, listSep)
        new JsonObject(flatMap)
    }

    Map<String, Object> flatten(Map<String, Object> cfgMap, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        flattenRecursively(cfgMap, '', keySep, listSep) as Map<String, Object>
    }

    JsonObject flattenToJson(Map<String, Object> cfgMap, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        new JsonObject(flatten(cfgMap, keySep, listSep))
    }

    Map<String, Object> flattenToMap(JsonObject cfgJson, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        flatten(cfgJson.map, keySep, listSep)
    }

    JsonObject unflatten(JsonObject cfgJson, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        def unflattenMap = unflattenRecursively(cfgJson.map, keySep, listSep)
        new JsonObject(unflattenMap)
    }

    Map<String, Object> unflatten(Map<String, Object> cfgMap, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        unflattenRecursively(cfgMap, keySep, listSep)
    }

    JsonObject unflattenToJson(Map<String, Object> cfgMap, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        new JsonObject(unflatten(cfgMap, keySep, listSep))
    }

    Map<String, Object> unflattenToMap(JsonObject cfgJson, String keySep = defaultMapKeySeparator,
            String listSep = defaultListJoinSeparator) {
        unflatten(cfgJson.map, keySep, listSep)
    }


    private def flattenRecursively(Map value, String prefix, String mapSep, String listSep) {
        value.inject([:]) { flatMap, e ->
            def nestedPrefix = prefix ? prefix + mapSep + e.key : e.key as String
            flatMap << flattenRecursively(e.value, nestedPrefix, mapSep, listSep)
        }
    }

    private def flattenRecursively(Object value, String prefix, String mapSep, String listSep) {
        [(prefix): value as String]
    }

    private def flattenRecursively(JsonObject value, String prefix, String mapSep, String listSep) {

    }

    private def flattenRecursively(Iterable value, String prefix, String mapSep, String listSep) {
        value.eachWithIndex { it, idx -> // scan for nested complex objects
            if (it instanceof Iterable || it instanceof Map) {
                throw new ComplexListConfigException(list: value, complexElement: it, complexElementIndex: idx,
                        key: prefix)
            }
        }
        [(prefix): value.join(listSep)]
    }

    private Map<String, Object> unflattenRecursively(Map<String, Object> map, String mapSep, String listSep) {
        map.inject(new LinkedHashMap<String, Object>()) { resMap, k, v ->
            def unflattenVal = unflattenRecursively(v, mapSep, listSep)
            def unflattenMap = buildMapFromComplexKey(k, unflattenVal, mapSep)
            def res = merge(resMap, unflattenMap)
            res
        }
    }

    private Map<String, Object> unflattenRecursively(JsonObject json, String mapSep, String listSep) {
        unflattenRecursively(json.map, mapSep, listSep)
    }

    private Map<String, Object> buildMapFromComplexKey(String complexKey, def val, String mapSep) {
        def keys = complexKey.split("\\${mapSep}")
        keys.reverse().inject(val) { acc, key ->
            [:].tap { it[key] = acc }
        } as Map
    }

    private Map<String, Object> merge(Map<String, Object> map1, Map<String, Object> map2) {
        mapHelper.mergeDeep(map1, map2, false)
    }

    private def unflattenRecursively(String str, String mapSep, String listSep) {
        def values = str.split("\\${listSep}")
        values.size() == 1 ? str : values
    }

    private def unflattenRecursively(Iterable iterable, String mapSep, String listSep) {
        throw new ListInPlainConfigException(value: iterable)
    }

    private def unflattenRecursively(def obj, String mapSep, String listSep) {
        obj
    }
}
