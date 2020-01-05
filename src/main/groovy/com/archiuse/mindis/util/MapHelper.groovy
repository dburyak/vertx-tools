package com.archiuse.mindis.util

import javax.inject.Singleton

@Singleton
class MapHelper {
    def <K, V> Map<K, V> mergeDeep(Map<K, V> map1, Map<K, V> map2, boolean concatCollections = false) {
        if (map1 == null || map2 == null) {
            throw new NullPointerException()
        }
        mergeDeepRecursively(map1, map2, concatCollections)
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
}
