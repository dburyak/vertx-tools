package com.archiuse.mindis.config

import com.archiuse.mindis.util.MapHelper
import io.vertx.core.json.JsonObject
import spock.lang.Specification

import static com.archiuse.mindis.config.ConfigHelper.defaultListJoinSeparator
import static com.archiuse.mindis.config.ConfigHelper.defaultMapKeySeparator

class ConfigHelperSpec extends Specification {
    ConfigHelper configHelper = new ConfigHelper()

    void setup() {
        configHelper.mapHelper = Stub(MapHelper) {
            mergeDeep([:], _ as Map, false) >> { args -> [:] + args[1] }
        }
    }

    def 'findAllWithPrefix wit default stripping'() {
        given: 'config json object'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'filter with default stripping'
        def filteredMap = configHelper.findAllWithPrefix(prefix, cfgJson)

        then: 'result map is expected'
        noExceptionThrown()
        filteredMap == expectedFiltered

        where:
        jsonDataMap                    | prefix || expectedFiltered
        [:]                            | ''     || [:]
        [:]                            | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | ''     || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'p1'   || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p2.k2': 'v2'] | 'p1'   || ['p1.k1': 'v1']
    }

    def 'findAllWithPrefix with explicit stripping'() {
        given: 'config json object'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'filter and strip if needed'
        def filteredMap = configHelper.findAllWithPrefix(prefix, cfgJson, stripPrefix)

        then: 'result map is as expected'
        noExceptionThrown()
        filteredMap == expectedFiltered

        where:
        jsonDataMap                    | prefix | stripPrefix || expectedFiltered
        [:]                            | ''     | false       || [:]
        [:]                            | ''     | true        || [:]

        [:]                            | 'some' | false       || [:]
        [:]                            | 'some' | true        || [:]

        ['p1.k1': 'v1', 'p1.k2': 'v2'] | ''     | false       || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | ''     | true        || ['p1.k1': 'v1', 'p1.k2': 'v2']

        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'some' | false       || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'some' | true        || [:]

        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'p1.'  | false       || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'p1.'  | true        || ['k1': 'v1', 'k2': 'v2']

        ['p1.k1': 'v1', 'p2.k2': 'v2'] | 'p1.'  | false       || ['p1.k1': 'v1']
        ['p1.k1': 'v1', 'p2.k2': 'v2'] | 'p1.'  | true        || ['k1': 'v1']
    }

    def 'findAllWithPrefixAndStrip'() {
        given: 'config json object'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'filter and strip'
        def filteredMap = configHelper.findAllWithPrefixAndStrip(prefix, cfgJson)

        then: 'result map is as expected, prefixes are stripped'
        noExceptionThrown()
        filteredMap == expectedFiltered

        where:
        jsonDataMap                    | prefix || expectedFiltered
        [:]                            | ''     || [:]
        [:]                            | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | ''     || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'p1.'  || ['k1': 'v1', 'k2': 'v2']
        ['p1.k1': 'v1', 'p2.k2': 'v2'] | 'p1.'  || ['k1': 'v1']
    }

    def 'flatten json'() {
        given: 'config json object'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'flatten to plain json object'
        def flatJson = keySep ? configHelper.flatten(cfgJson, keySep)
                : configHelper.flatten(cfgJson)

        then: 'result flat json is flattened correctly'
        noExceptionThrown()
        flatJson == new JsonObject(expectedFlatJsonMap)

        where:
        jsonDataMap                  | keySep || expectedFlatJsonMap
        [:]                          | null   || [:]
        ['k1.k2': 'v1']              | null   || ['k1.k2': 'v1']
        ['k1': ['k2': 'v1']]         | null   || ['k1.k2': 'v1']
        ['k1': ['k2': ['k3': 'v1']]] | null   || ['k1.k2.k3': 'v1']
        [:]                          | ':'    || [:]
        ['k1.k1': 'v1']              | ':'    || ['k1.k1': 'v1']
        ['k1': ['k1': 'v1']]         | ':'    || ['k1:k1': 'v1']
        ['k1': ['k2': ['k3': 'v1']]] | '_'    || ['k1_k2_k3': 'v1']
    }

    def 'flatten json with nested lists'() {
        given: 'config json object with nested lists, each list element is plain data'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'flatten to plain json object'
        def flatJson = listSep ? configHelper.flatten(cfgJson, defaultMapKeySeparator, listSep)
                : configHelper.flatten(cfgJson)

        then: 'result flat json is flattened correctly'
        noExceptionThrown()
        flatJson == new JsonObject(expectedFlatJsonMap)

        where:
        jsonDataMap                                        | listSep || expectedFlatJsonMap
        [k1: [], k2: [k3: []]]                             | ':'     || [k1: '', 'k2.k3': '']
        [k1: [1, 2, 'a', 'b'], k2: [k3: [3, 4, 'c', 'd']]] | null    || [k1: '1,2,a,b', 'k2.k3': '3,4,c,d']
        [k1: [1, 2, 'a', 'b'], k2: [k3: [3, 4, 'c', 'd']]] | '|'     || [k1: '1|2|a|b', 'k2.k3': '3|4|c|d']
    }

    def 'flatten json throws for nested complex lists'() {
        given: 'config json object with complex objects nested in arrays'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'flatten to plain json object'
        configHelper.flatten(cfgJson)

        then: 'exception is thrown with correct complex object captured'
        def ex = thrown ComplexListConfigException
        ex.list == eList
        ex.complexElement == exElement
        ex.complexElementIndex == exIdx

        where:
        jsonDataMap << [
                [k1: ['a', ['b', 'c']]],
                [k1: [k2: 'v2', k4: ['a', 2, 'c', [k5: 'd', k6: 'e']]]]
        ]
        eList << [
                ['a', ['b', 'c']],
                ['a', 2, 'c', [k5: 'd', k6: 'e']]
        ]
        exElement << [
                ['b', 'c'],
                [k5: 'd', k6: 'e']
        ]
        exIdx << [
                1,
                3
        ]
    }

    def 'flatten map'() {
        given: 'config map'
        when: 'flatten to plain map'
        def flatMap = keySep ? configHelper.flatten(cfgMap, keySep)
                : configHelper.flatten(cfgMap)

        then: 'result flat map is flattened correctly'
        noExceptionThrown()
        flatMap == expectedFlatMap

        where:
        cfgMap                       | keySep || expectedFlatMap
        [:]                          | null   || [:]
        ['k1.k2': 'v1']              | null   || ['k1.k2': 'v1']
        ['k1': ['k2': 'v1']]         | null   || ['k1.k2': 'v1']
        ['k1': ['k2': ['k3': 'v1']]] | null   || ['k1.k2.k3': 'v1']
        [:]                          | ':'    || [:]
        ['k1.k1': 'v1']              | ':'    || ['k1.k1': 'v1']
        ['k1': ['k1': 'v1']]         | ':'    || ['k1:k1': 'v1']
        ['k1': ['k2': ['k3': 'v1']]] | '_'    || ['k1_k2_k3': 'v1']
    }

    def 'flatten map with nested lists'() {
        given: 'config map with nested lists, each list element is plain'
        when: 'flatten to plain map'
        def flatMap = listSep ? configHelper.flatten(cfgMap, defaultMapKeySeparator, listSep)
                : configHelper.flatten(cfgMap)

        then: 'result flat map is flattened correctly'
        noExceptionThrown()
        flatMap == expectedFlatMap

        where:
        cfgMap                                             | listSep || expectedFlatMap
        [k1: [], k2: [k3: []]]                             | ':'     || [k1: '', 'k2.k3': '']
        [k1: [1, 2, 'a', 'b'], k2: [k3: [3, 4, 'c', 'd']]] | null    || [k1: '1,2,a,b', 'k2.k3': '3,4,c,d']
        [k1: [1, 2, 'a', 'b'], k2: [k3: [3, 4, 'c', 'd']]] | '|'     || [k1: '1|2|a|b', 'k2.k3': '3|4|c|d']
    }

    def 'flatten map throws for nested complex lists'() {
        given: 'config map with complex objects nested in lists'
        when: 'flatten ot plain map'
        configHelper.flatten(cfgMap)

        then: 'exception is thrown with correct complex object captured'
        def ex = thrown ComplexListConfigException
        ex.list == eList
        ex.complexElement == exElement
        ex.complexElementIndex == exIdx

        where:
        cfgMap << [
                [k1: ['a', ['b', 'c']]],
                [k1: [k2: 'v2', k4: ['a', 2, 'c', [k5: 'd', k6: 'e']]]]
        ]
        eList << [
                ['a', ['b', 'c']],
                ['a', 2, 'c', [k5: 'd', k6: 'e']]
        ]
        exElement << [
                ['b', 'c'],
                [k5: 'd', k6: 'e']
        ]
        exIdx << [
                1,
                3
        ]
    }

    def 'flattenToJson from map delegates to flatten from map'() {
        given: 'cfg map and config util'
        def cfgMap = [not_used: 'map']
        configHelper = Spy(configHelper)
        def keySep1 = '.'
        def keySep2 = ':'
        def listSep2 = '|'

        when: 'flatten cfg map to json'
        configHelper.flattenToJson(cfgMap)
        configHelper.flattenToJson(cfgMap, keySep1)
        configHelper.flattenToJson(cfgMap, keySep2, listSep2)

        then: 'call is delegated to flatten from map to map impl'
        noExceptionThrown()
        1 * configHelper.flatten(cfgMap, defaultMapKeySeparator, defaultListJoinSeparator)
        1 * configHelper.flatten(cfgMap, keySep1, defaultListJoinSeparator)
        1 * configHelper.flatten(cfgMap, keySep2, listSep2)
    }

    def 'flattenToMap from json delegates to flatten from map'() {
        given: 'json cfg'
        def cfgMap = [not_used: 'val']
        def jsonCfg = new JsonObject(cfgMap)
        configHelper = Spy(configHelper)
        def keySep1 = '.'
        def keySep2 = ':'
        def listSep2 = '|'

        when: 'flatten json cfg ot map'
        configHelper.flattenToMap(jsonCfg)
        configHelper.flattenToMap(jsonCfg, keySep1)
        configHelper.flattenToMap(jsonCfg, keySep2, listSep2)

        then: 'call is delegated to flatten from map to map impl'
        noExceptionThrown()
        1 * configHelper.flatten(cfgMap, defaultMapKeySeparator, defaultListJoinSeparator)
        1 * configHelper.flatten(cfgMap, keySep1, defaultListJoinSeparator)
        1 * configHelper.flatten(cfgMap, keySep2, listSep2)
    }

    def 'unflatten json'() {
        given: 'flat json'
        def cfgJson = new JsonObject(flatJsonMap)

        when: 'unflatten json'
        def unflattenJson = keySep ? configHelper.unflatten(cfgJson, keySep)
                : configHelper.unflatten(cfgJson)

        then: 'result non-flat json is as expected'
        noExceptionThrown()
        unflattenJson == new JsonObject(expectedUnflattenMap)

        where:
        flatJsonMap        | keySep || expectedUnflattenMap
        [:]                | null   || [:]
        ['k1.k2': 'v1']    | null   || ['k1': ['k2': 'v1']]
        ['k1.k2.k3': 'v1'] | null   || ['k1': ['k2': ['k3': 'v1']]]
        [:]                | ':'    || [:]
        ['k1.k1': 'v1']    | ':'    || ['k1.k1': 'v1']
        ['k1:k1': 'v1']    | ':'    || ['k1': ['k1': 'v1']]
        ['k1_k2_k3': 'v1'] | '_'    || ['k1': ['k2': ['k3': 'v1']]]
    }

    def 'unflatten json with nested lists'() {
        given: 'flat json with nested lists'
        def cfgJson = new JsonObject(flatJsonMap)
        with(configHelper.mapHelper) {
            mergeDeep([k1: ''], [k2: [k3: '']], false) >> [k1: '', k2: [k3: '']]
            mergeDeep([k1: ['1', '2', 'a', 'b']], [k2: [k3: ['3', '4', 'c', 'd']]], false) >>
                    [k1: ['1', '2', 'a', 'b'], k2: [k3: ['3', '4', 'c', 'd']]]
        }

        when: 'unflatten json'
        def unflattenJson = listSep ? configHelper.unflatten(cfgJson, defaultMapKeySeparator, listSep)
                : configHelper.unflatten(cfgJson)

        then: 'result non-flat json is as expected'
        new JsonObject(expectedUnflattenMap) == new JsonObject(expectedUnflattenMap)
        noExceptionThrown()
        new JsonObject(unflattenJson.encode()) == new JsonObject(new JsonObject(expectedUnflattenMap).encode())

        where:
        flatJsonMap                         | listSep || expectedUnflattenMap
        [k1: '', 'k2.k3': '']               | ':'     || [k1: '', k2: [k3: '']]
        [k1: '1,2,a,b', 'k2.k3': '3,4,c,d'] | null    || [k1: ['1', '2', 'a', 'b'], k2: [k3: ['3', '4', 'c', 'd']]]
        [k1: '1|2|a|b', 'k2.k3': '3|4|c|d'] | '|'     || [k1: ['1', '2', 'a', 'b'], k2: [k3: ['3', '4', 'c', 'd']]]
    }

    def 'unflatten map'() {
        given: 'flat map'
        with(configHelper.mapHelper) {
            mergeDeep(['k1': ['k2': 'v2']], [k1: [k3: 'v2']], false) >> [k1: [k2: 'v2', k3: 'v2']]
            mergeDeep(['k1': ['k2': 'v2']], [k1: [k3: 'v3']], false) >> [k1: [k2: 'v2', k3: 'v3']]
            mergeDeep(['k1': ['k2': ['k3': 'v3']]], ['k1': ['k2': ['k4': 'v4']]], false) >>
                    [k1: [k2: [k3: 'v3', k4: 'v4']]]
        }

        when: 'unflatten map'
        def unflattenMap = keySep ? configHelper.unflatten(flatMap, keySep)
                : configHelper.unflatten(flatMap)

        then: 'result non-flat map is as expected'
        noExceptionThrown()
        unflattenMap == expectedUnflattenMap

        where:
        flatMap                              | keySep || expectedUnflattenMap
        [:]                                  | null   || [:]
        ['k1.k2': 'v1']                      | null   || ['k1': ['k2': 'v1']]
        ['k1.k2.k3': 'v1']                   | null   || ['k1': ['k2': ['k3': 'v1']]]
        ['k1.k2': 'v2', 'k1.k3': 'v2']       | null   || [k1: [k2: 'v2', k3: 'v2']]
        ['k1.k2': 'v2', 'k1.k3': 'v3']       | null   || [k1: [k2: 'v2', k3: 'v3']]
        ['k1.k2.k3': 'v3', 'k1.k2.k4': 'v4'] | null   || [k1: [k2: [k3: 'v3', k4: 'v4']]]
        [:]                                  | ':'    || [:]
        ['k1.k1': 'v1']                      | ':'    || ['k1.k1': 'v1']
        ['k1:k1': 'v1']                      | ':'    || ['k1': ['k1': 'v1']]
        ['k1_k2_k3': 'v1']                   | '_'    || ['k1': ['k2': ['k3': 'v1']]]
    }

    def 'unflatten map with nested lists'() {
        given: 'flat comfig map with nested lists'
        with(configHelper.mapHelper) {
            mergeDeep([k1: ''], [k2: [k3: '']], false) >> [k1: '', k2: [k3: '']]
            mergeDeep([k1: ['1', '2', 'a', 'b']], [k2: [k3: ['3', '4', 'c', 'd']]], false) >>
                    [k1: ['1', '2', 'a', 'b'], k2: [k3: ['3', '4', 'c', 'd']]]
        }

        when: 'unflatten map'
        def unflattenMap = listSep ? configHelper.unflatten(flatMap, defaultMapKeySeparator, listSep)
                : configHelper.unflatten(flatMap)

        then: 'result non-flat map is as expected'
        noExceptionThrown()
        unflattenMap == expectedUnflattenMap

        where:
        flatMap                             | listSep || expectedUnflattenMap
        [k1: '', 'k2.k3': '']               | ':'     || [k1: '', k2: [k3: '']]
        [k1: '1,2,a,b', 'k2.k3': '3,4,c,d'] | null    || [k1: ['1', '2', 'a', 'b'], k2: [k3: ['3', '4', 'c', 'd']]]
        [k1: '1|2|a|b', 'k2.k3': '3|4|c|d'] | '|'     || [k1: ['1', '2', 'a', 'b'], k2: [k3: ['3', '4', 'c', 'd']]]
    }

    def 'unflattenToJson from map delegates to unflatten from map'() {
        given: 'flat map'
        def cfgMap = [not_used: 'map']
        configHelper = Spy(configHelper)
        def keySep1 = '.'
        def keySep2 = ':'
        def listSep2 = '|'

        when: 'unflattem map to json'
        configHelper.unflattenToJson(cfgMap)
        configHelper.unflattenToJson(cfgMap, keySep1)
        configHelper.unflattenToJson(cfgMap, keySep2, listSep2)

        then: 'call is delegated to unflatten from json to json impl'
        noExceptionThrown()
        1 * configHelper.unflatten(cfgMap, defaultMapKeySeparator, defaultListJoinSeparator)
        1 * configHelper.unflatten(cfgMap, keySep1, defaultListJoinSeparator)
        1 * configHelper.unflatten(cfgMap, keySep2, listSep2)
    }

    def 'unflattenToMap from json delegates to unflatten from map'() {
        given: 'flat json config'
        def cfgMap = [not_used: 'json_cfg']
        def jsonCfg = new JsonObject(cfgMap)
        configHelper = Spy(configHelper)
        def keySep1 = '.'
        def keySep2 = ':'
        def listSep2 = '|'

        when: 'unflatten json to map'
        configHelper.unflattenToMap(jsonCfg)
        configHelper.unflattenToMap(jsonCfg, keySep1)
        configHelper.unflattenToMap(jsonCfg, keySep2, listSep2)

        then: 'call is delegated to unflatten from map to map impl'
        noExceptionThrown()
        1 * configHelper.unflatten(cfgMap, defaultMapKeySeparator, defaultListJoinSeparator)
        1 * configHelper.unflatten(cfgMap, keySep1, defaultListJoinSeparator)
        1 * configHelper.unflatten(cfgMap, keySep2, listSep2)
    }
}
