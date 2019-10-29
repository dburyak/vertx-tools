package com.archiuse.mindis.tools.vertx

import io.vertx.core.json.JsonObject
import spock.lang.Specification
import spock.lang.Unroll

class ConfigHelperSpec extends Specification {
    ConfigHelper configHelper

    void setup() {
        configHelper = new ConfigHelper()
    }

    @Unroll
    def 'test findAllWithPrefix wit default stripping'() {
        given: 'config json object'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'filter with default stripping'
        def filteredMap = configHelper.findAllWithPrefix(prefix, cfgJson)

        then: 'result map is expected'
        noExceptionThrown()
        assert filteredMap == expectedFiltered

        where:
        jsonDataMap                    | prefix || expectedFiltered
        [:]                            | ''     || [:]
        [:]                            | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | ''     || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'p1'   || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p2.k2': 'v2'] | 'p1'   || ['p1.k1': 'v1']
    }

    @Unroll
    def 'test findAllWithPrefix with explicit stripping'() {
        given: 'config json object'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'filter and strip if needed'
        def filteredMap = configHelper.findAllWithPrefix(prefix, cfgJson, stripPrefix)

        then: 'result map is as expected'
        noExceptionThrown()
        assert filteredMap == expectedFiltered

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

    @Unroll
    def 'test findAllWithPrefixAndStrip'() {
        given: 'config json object'
        def cfgJson = new JsonObject(jsonDataMap)

        when: 'filter and strip'
        def filteredMap = configHelper.findAllWithPrefixAndStrip(prefix, cfgJson)

        then: 'result map is as expected, prefixes are stripped'
        noExceptionThrown()
        assert filteredMap == expectedFiltered

        where:
        jsonDataMap                    | prefix || expectedFiltered
        [:]                            | ''     || [:]
        [:]                            | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | ''     || ['p1.k1': 'v1', 'p1.k2': 'v2']
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'some' || [:]
        ['p1.k1': 'v1', 'p1.k2': 'v2'] | 'p1.'  || ['k1': 'v1', 'k2': 'v2']
        ['p1.k1': 'v1', 'p2.k2': 'v2'] | 'p1.'  || ['k1': 'v1']
    }
}
