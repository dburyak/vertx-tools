package com.archiuse.mindis.util

import spock.lang.Specification

class MapHelperSpec extends Specification {
    MapHelper mapHelper = new MapHelper()

    def 'mergeDeep'() {
        when: 'deeply merge two maps'
        def resultMap = concat ? mapHelper.mergeDeep(map1, map2, concat)
                : mapHelper.mergeDeep(map1, map2)

        then: 'maps are merged correctly'
        noExceptionThrown()
        resultMap == expectedResultMap

        where:
        map1               | map2               | concat || expectedResultMap
        [:]                | [:]                | null   || [:]
        [k1: 'v1']         | [k2: 'v2']         | null   || [k1: 'v1', k2: 'v2']
        [k1: 'v1']         | [k1: 'v2']         | null   || [k1: 'v2']
        [k1: [k2: 'v2']]   | [k1: [k3: 'v3']]   | null   || [k1: [k2: 'v2', k3: 'v3']]
        [k1: [k2: 1]]      | [k1: [k2: 2]]      | null   || [k1: [k2: 2]]
        [k1: [1, 2]]       | [k1: [3, 4]]       | null   || [k1: [3, 4]]
        [k1: [1, 2]]       | [k1: [3, 4]]       | true   || [k1: [1, 2, 3, 4]]
        [k1: [k2: [1, 2]]] | [k1: [k2: [3, 4]]] | null   || [k1: [k2: [3, 4]]]
        [k1: [k2: [1, 2]]] | [k1: [k2: [3, 4]]] | true   || [k1: [k2: [1, 2, 3, 4]]]
        [:]                | [:]                | true   || [:]
        [:]                | [:]                | false  || [:]
        [:]                | [k1: 'v1']         | false  || [k1: 'v1']
        [k1: 'v1']         | [:]                | false  || [k1: 'v1']
        [:]                | [k1: 'v1']         | true   || [k1: 'v1']
        [k1: 'v1']         | [:]                | true   || [k1: 'v1']
        [k1: [k2: 'v2']]   | [:]                | false  || [k1: [k2: 'v2']]
        [:]                | [k1: [k2: 'v2']]   | false  || [k1: [k2: 'v2']]
        [k1: [k2: 'v2']]   | [:]                | true   || [k1: [k2: 'v2']]
        [:]                | [k1: [k2: 'v2']]   | true   || [k1: [k2: 'v2']]

    }

    def 'mergeDeep throws NPE when either param is null'() {
        when: 'deeply merge maps with one of them being null'
        mapHelper.mergeDeep(map1, map2)

        then: 'NPE it thrown'
        thrown NullPointerException

        where:
        map1 | map2
        [:]  | null
        null | [:]
    }
}
