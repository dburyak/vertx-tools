package com.archiuse.mindis.util

import io.vertx.core.MultiMap

import javax.inject.Singleton

@Singleton
class MapHelper {
    def <K, V> Map<K, V> mergeDeep(Map<K, V> map1, Map<K, V> map2, boolean concatCollections = false) {
        if (map1 == null || map2 == null) {
            throw new NullPointerException()
        }
        mergeDeepRecursively(map1, map2, concatCollections)
    }

    def <K, V> boolean containsAll(Map<K, V> map, Map<K, V> subMap) {
        def mapEntries = map?.entrySet() ?: []
        subMap?.every { it in mapEntries }
    }

    def <K, V> MultiMap addToMultiMap(MultiMap multiMap, Map<K, V> map) {
        map.each { k, v ->
            if (v instanceof Map) {
                throw new IllegalArgumentException('nested maps are not supported in vertx multimaps')
            }
            addValToMultiMap multiMap, k, v
        }
        multiMap
    }

    def <K, V> io.vertx.reactivex.core.MultiMap addToMultiMap(io.vertx.reactivex.core.MultiMap multiMap,
            Map<K, V> map) {
        addToMultiMap multiMap.delegate, map
        multiMap
    }

    Map<String, List<String>> toMap(MultiMap multiMap) {
        multiMap.entries().inject(new LinkedHashMap<String, List<String>>()) { resMap, e ->
            resMap.computeIfAbsent(e.key) { [] } << e.value
            resMap
        }
    }

    Map<String, List<String>> toMap(io.vertx.reactivex.core.MultiMap multiMap) {
        toMap multiMap.delegate
    }

    private <K, V> Map<K, V> mergeDeepRecursively(Map<K, V> map1, Map<K, V> map2, boolean concatCollections) {
        map2.inject(map1 ? new LinkedHashMap<K, V>(map1) : new LinkedHashMap<K, V>()) { res, k2, v2 ->
            res[k2] = mergeDeepRecursively(res[k2], v2, concatCollections)
            res
        }
    }

    private def mergeDeepRecursively(def obj1, def obj2, boolean concatCollections) {
        obj2
    }

    private def mergeDeepRecursively(Collection collection1, Collection collection2, boolean concatCollections) {
        concatCollections
                ? collection1 + collection2
                : collection2
    }

    private void addValToMultiMap(MultiMap multiMap, Object key, Object value) {
        multiMap.add key as String, value as String
    }

    private void addValToMultiMap(MultiMap multiMap, Object key, Iterable values) {
        values.each {
            multiMap.add key as String, it as String
        }
    }
}
