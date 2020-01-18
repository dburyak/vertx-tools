package com.archiuse.mindis.json

import groovy.transform.EqualsAndHashCode
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import spock.lang.Specification

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

import static java.time.Month.JULY
import static java.time.Month.JUNE

class JsonHelperSpec extends Specification {
    private static enum TestEnum {
        FIRST, SECOND, THIRD
    }

    static final def instant = Instant.parse('2019-12-08T22:07:40.682937Z')
    static final def duration = Duration.ofSeconds(3)
    static final def localDate = LocalDate.of(1989, JULY, 27)
    static final def localDateTime = LocalDateTime.of(1989, JULY, 27, 15, 33, 17, 459)
    static final def localTime = LocalTime.of(3, 53, 28, 234_567_890)
    static final def zonedDateTime = ZonedDateTime.of(LocalDate.of(1989, JULY, 27), LocalTime.of(7, 3, 25, 83),
            ZoneId.of('Europe/Paris'))
    static final def offsetDateTime = OffsetDateTime.of(LocalDateTime.of(1900, JUNE, 13, 12, 29, 3, 431),
            ZoneOffset.ofHours(-4))
    static final def offsetTime = OffsetTime.of(19, 37, 0, 34576, ZoneOffset.ofHours(7))

    JsonHelper jsonHelper = new JsonHelper()

    void 'toJson from map with special types encoding'() {
        given: 'map'
        def encodeSpecial = true

        when: 'build json from map'
        def actualJson = jsonHelper.toJson map, encodeSpecial

        then: 'json is built from map correctly, types are detected correctly'
        noExceptionThrown()
        new JsonObject(actualJson.encode()) == new JsonObject(expectedJson)

        where:
        map                           || expectedJson
        [:]                           || '{}'
        [k1: 'v1']                    || '{"k1": "v1"}'
        [k2: 200]                     || '{"k2": 200}'
        [k3: 20.3]                    || '{"k3": "20.3"}'
        [k4: (21.4).floatValue()]     || '{"k4": "21.4"}'
        [k5: (22.5).doubleValue()]    || '{"k5": "22.5"}'
        [k21: 234G]                   || '{"k21": "234"}' // BigInteger
        [k6: null]                    || '{"k6": null}'
        [k7: false, k8: Boolean.TRUE] || '{"k7": false, "k8": true}'
        [k9: new byte[] {3, 4, 5}]    || '{"k9": ' +
                '{"special_type": "byte_array", "special_value": "AwQF"}}'
        [k10: [1, 2, 3]]              || '{"k10": [1, 2, 3]}'
        [k11: ['a', 2, 3.55]]         || '{"k11": ["a", 2, "3.55"]}'
        [k12: instant]                || '{"k12": "2019-12-08T22:07:40.682937Z"}'
        [k13: duration]               || '{"k13": "PT3S"}'
        [k14: localDate]              || '{"k14": "1989-07-27"}'
        [k15: localDateTime]          || '{"k15": "1989-07-27T15:33:17.000000459"}'
        [k16: localTime]              || '{"k16": "03:53:28.234567890"}'
        [k17: zonedDateTime]          || '{"k17": "1989-07-27T07:03:25.000000083+02:00[Europe/Paris]"}'
        [k18: offsetDateTime]         || '{"k18": "1900-06-13T12:29:03.000000431-04:00"}'
        [k19: offsetTime]             || '{"k19": "19:37:00.000034576+07:00"}'
        [k20: TestEnum.SECOND]        || '{"k20": ' +
                '{"special_type": "enum", "special_enum_class": "com.archiuse.mindis.json.JsonHelperSpec$TestEnum",' +
                '"special_value": "SECOND"}}'
    }

    void 'toJson from map with NO special types encoding'() {
        given: 'map'
        def encodeSpecial = false

        when: 'build json from map'
        def actualJson = jsonHelper.toJson map, encodeSpecial

        then: 'json is built from map correctly, types are detected correctly'
        noExceptionThrown()
        new JsonObject(actualJson.encode()) == new JsonObject(expectedJson)

        where:
        map                           || expectedJson
        [:]                           || '{}'
        [k1: 'v1']                    || '{"k1": "v1"}'
        [k2: 200]                     || '{"k2": 200}'
        [k3: 20.3]                    || '{"k3": "20.3"}'
        [k4: (21.4).floatValue()]     || '{"k4": 21.4}'
        [k5: (22.5).doubleValue()]    || '{"k5": 22.5}'
        [k21: 234G]                   || '{"k21": "234"}'  // BigInteger
        [k6: null]                    || '{"k6": null}'
        [k7: false, k8: Boolean.TRUE] || '{"k7": false, "k8": true}'
        [k9: new byte[] {3, 4, 5}]    || '{"k9": "AwQF"}'
        [k10: [1, 2, 3]]              || '{"k10": [1, 2, 3]}'
        [k11: ['a', 2, 3.55]]         || '{"k11": ["a", 2, "3.55"]}'
        [k12: instant]                || '{"k12": "2019-12-08T22:07:40.682937Z"}'
        [k13: duration]               || '{"k13": "PT3S"}'
        [k14: localDate]              || '{"k14": "1989-07-27"}'
        [k15: localDateTime]          || '{"k15": "1989-07-27T15:33:17.000000459"}'
        [k16: localTime]              || '{"k16": "03:53:28.234567890"}'
        [k17: zonedDateTime]          || '{"k17": "1989-07-27T07:03:25.000000083+02:00[Europe/Paris]"}'
        [k18: offsetDateTime]         || '{"k18": "1900-06-13T12:29:03.000000431-04:00"}'
        [k19: offsetTime]             || '{"k19": "19:37:00.000034576+07:00"}'
        [k20: TestEnum.SECOND]        || '{"k20": "SECOND"}'
    }

    void 'toJson from null returns null'() {
        given:
        def nullObj = null

        when:
        def json = jsonHelper.toJson nullObj

        then:
        noExceptionThrown()
        json == null
    }

    void 'toJson from unsupported type throws exception'() {
        given:
        BigDecimal bigDecimal = 3.56

        when:
        jsonHelper.toJson bigDecimal

        then:
        thrown IllegalArgumentException
    }

    void 'toJson from iterable with special types encoding'() {
        given: 'iterable'
        def encodeSpecial = true

        when: 'build json from iterable'
        def actualJson = jsonHelper.toJson iterable, encodeSpecial

        then: 'json is built from iterable correctly, types are detected correctly'
        noExceptionThrown()
        new JsonArray(actualJson.encode()) == new JsonArray(expectedJson)

        where:
        iterable                 || expectedJson
        []                       || '[]'
        ['v1']                   || '["v1"]'
        [200]                    || '[200]'
        [20.3]                   || '["20.3"]'
        [(21.4).floatValue()]    || '["21.4"]'
        [(22.5).doubleValue()]   || '["22.5"]'
        [234G]                   || '["234"]'  // BigInteger
        [null]                   || '[null]'
        [false, Boolean.TRUE]    || '[false, true]'
        [new byte[] {3, 4, 5}]   || '[{"special_type": "byte_array", "special_value": "AwQF"}]'
        [[1, 2, 3]]              || '[[1, 2, 3]]'
        [15, ['a', 2, 3.55], 17] || '[15, ["a", 2, "3.55"], 17]'
        [instant]                || '["2019-12-08T22:07:40.682937Z"]'
        [duration]               || '["PT3S"]'
        [localDate]              || '["1989-07-27"]'
        [localDateTime]          || '["1989-07-27T15:33:17.000000459"]'
        [localTime]              || '["03:53:28.234567890"]'
        [zonedDateTime]          || '["1989-07-27T07:03:25.000000083+02:00[Europe/Paris]"]'
        [offsetDateTime]         || '["1900-06-13T12:29:03.000000431-04:00"]'
        [offsetTime]             || '["19:37:00.000034576+07:00"]'
        [TestEnum.SECOND]        || '[{"special_type": "enum", ' +
                '"special_enum_class": "com.archiuse.mindis.json.JsonHelperSpec$TestEnum", ' +
                '"special_value": "SECOND"}]'
    }

    void 'toJson from iterable with NO special types encoding'() {
        given: 'iterable'
        def encodeSpecial = false

        when: 'build json from iterable'
        def actualJson = jsonHelper.toJson iterable, encodeSpecial

        then: 'json is built from iterable correctly, types are detected correctly'
        noExceptionThrown()
        new JsonArray(actualJson.encode()) == new JsonArray(expectedJson)

        where:
        iterable                 || expectedJson
        []                       || '[]'
        ['v1']                   || '["v1"]'
        [200]                    || '[200]'
        [20.3]                   || '["20.3"]'
        [(21.4).floatValue()]    || '[21.4]'
        [(22.5).doubleValue()]   || '[22.5]'
        [234G]                   || '["234"]'  // BigInteger
        [null]                   || '[null]'
        [false, Boolean.TRUE]    || '[false, true]'
        [new byte[] {3, 4, 5}]   || '["AwQF"]'
        [[1, 2, 3]]              || '[[1, 2, 3]]'
        [15, ['a', 2, 3.55], 17] || '[15, ["a", 2, "3.55"], 17]'
        [instant]                || '["2019-12-08T22:07:40.682937Z"]'
        [duration]               || '["PT3S"]'
        [localDate]              || '["1989-07-27"]'
        [localDateTime]          || '["1989-07-27T15:33:17.000000459"]'
        [localTime]              || '["03:53:28.234567890"]'
        [zonedDateTime]          || '["1989-07-27T07:03:25.000000083+02:00[Europe/Paris]"]'
        [offsetDateTime]         || '["1900-06-13T12:29:03.000000431-04:00"]'
        [offsetTime]             || '["19:37:00.000034576+07:00"]'
        [TestEnum.SECOND]        || '["SECOND"]'
    }

    void 'toMap from json object to map with special types decoded'() {
        given: 'json'
        def decodeSpecial = true
        def jsonObj = new JsonObject(json)

        when: 'parse json to map and decode special objects'
        def actualMap = jsonHelper.toMap jsonObj, decodeSpecial

        then: 'map is parsed correctly, types are detected correctly'
        noExceptionThrown()
        actualMap == expectedMap

        where:
        json                                                           || expectedMap
        '{}'                                                           || [:]
        '{"k1": "v1"}'                                                 || [k1: 'v1']
        '{"k2": 200}'                                                  || [k2: 200]
        '{"k3": "20.3"}'                                               || [k3: 20.3G]
        '{"k4": 21.4}'                                                 || [k4: 21.4]
        '{"k21": "345"}'                                               || [k21: 345G]
        '{"k6": null}'                                                 || [k6: null]
        '{"k7": false, "k8": true}'                                    || [k7: false, k8: true]
        '{"k9": {"special_type": "byte_array", ' +
                '"special_value": "AwQF"}}'                            || [k9: new byte[] {3, 4, 5}]
        '{"k10": [1, 2, 3]}'                                           || [k10: [1, 2, 3]]
        '{"k11": ["a", 2, "3.55"]}'                                    || [k11: ['a', 2, 3.55]]
        '{"k12": "2019-12-08T22:07:40.682937Z"}'                       || [k12: instant]
        '{"k13": "PT3S"}'                                              || [k13: duration]
        '{"k14": "1989-07-27"}'                                        || [k14: localDate]
        '{"k15": "1989-07-27T15:33:17.000000459"}'                     || [k15: localDateTime]
        '{"k16": "03:53:28.234567890"}'                                || [k16: localTime]
        '{"k17": "1989-07-27T07:03:25.000000083+02:00[Europe/Paris]"}' || [k17: zonedDateTime]
        '{"k18": "1900-06-13T12:29:03.000000431-04:00"}'               || [k18: offsetDateTime]
        '{"k19": "19:37:00.000034576+07:00"}'                          || [k19: offsetTime]
        '{"k20": {"special_type": "enum", "special_enum_class": ' +
                '"com.archiuse.mindis.json.JsonHelperSpec$TestEnum", ' +
                '"special_value": "SECOND"}}'                          || [k20: TestEnum.SECOND]
    }

    void 'toMap from json object to map with NO special types decoding'() {
        given: 'json'
        def decodeSpecial = false
        def jsonObj = new JsonObject(json)

        when: 'parse json to map and decode special objects'

        def actualMap = jsonHelper.toMap jsonObj, decodeSpecial

        then: 'map is parsed correctly, types are detected correctly'
        noExceptionThrown()
        actualMap == expectedMap

        where:
        json                                                           || expectedMap
        '{}'                                                           || [:]
        '{"k1": "v1"}'                                                 || [k1: 'v1']
        '{"k2": 200}'                                                  || [k2: 200]
        '{"k3": "20.3"}'                                               || [k3: 20.3G]
        '{"k4": 21.4}'                                                 || [k4: 21.4]
        '{"k6": null}'                                                 || [k6: null]
        '{"k7": false, "k8": true}'                                    || [k7: false, k8: true]
        '{"k9": {"special_type": "byte_array", ' +
                '"special_value": "AwQF"}}'                            || [k9: [special_type : 'byte_array',
                                                                                special_value: 'AwQF']]
        '{"k10": [1, 2, 3]}'                                           || [k10: [1, 2, 3]]
        '{"k11": ["a", 2, "3.55"]}'                                    || [k11: ['a', 2, 3.55G]]
        '{"k12": "2019-12-08T22:07:40.682937Z"}'                       || [k12: instant]
        '{"k13": "PT3S"}'                                              || [k13: duration]
        '{"k14": "1989-07-27"}'                                        || [k14: localDate]
        '{"k15": "1989-07-27T15:33:17.000000459"}'                     || [k15: localDateTime]
        '{"k16": "03:53:28.234567890"}'                                || [k16: localTime]
        '{"k17": "1989-07-27T07:03:25.000000083+02:00[Europe/Paris]"}' || [k17: zonedDateTime]
        '{"k18": "1900-06-13T12:29:03.000000431-04:00"}'               || [k18: offsetDateTime]
        '{"k19": "19:37:00.000034576+07:00"}'                          || [k19: offsetTime]
        '{"k20": {"special_type": "enum", "special_enum_class": ' +
                '"com.archiuse.mindis.json.JsonHelperSpec$TestEnum", ' +
                '"special_value": "SECOND"}}'                          || [k20: [special_type      : 'enum',
                                                                                 special_enum_class: 'com.archiuse' +
                                                                                         '.mindis.json' +
                                                                                         '.JsonHelperSpec$TestEnum',
                                                                                 special_value     : 'SECOND']]
        '{"k21": "345"}'                                               || [k21: 345G]
    }

    void 'toList from json array with special types decoding'() {
        given: 'json array'
        def decodeSpecial = true
        def jsonArray = new JsonArray(jsonStr)

        when: 'parse json array and decode special objects'
        def actualList = jsonHelper.toList jsonArray, decodeSpecial

        then: 'list is parsed correctly, types are detected correctly'
        noExceptionThrown()
        actualList == expectedList

        where:
        jsonStr                                                     || expectedList
        '[]'                                                        || []
        '["v1"]'                                                    || ['v1']
        '[200]'                                                     || [200]
        '["20.3"]'                                                  || [20.3G]
        '[21.4]'                                                    || [21.4]
        '["345"]'                                                   || [345G]
        '[null]'                                                    || [null]
        '[false, true]'                                             || [false, true]
        '[{"special_type": "byte_array", "special_value": "AwQF"}]' || [new byte[] {3, 4, 5}]
        '[[1, 2], [2, 3, 4]]'                                       || [[1, 2], [2, 3, 4]]
        '["a", 2, "3.55"]'                                          || ['a', 2, 3.55G]
        '["2019-12-08T22:07:40.682937Z"]'                           || [instant]
        '["PT3S"]'                                                  || [duration]
        '["1989-07-27"]'                                            || [localDate]
        '["1989-07-27T15:33:17.000000459"]'                         || [localDateTime]
        '["03:53:28.234567890"]'                                    || [localTime]
        '["1989-07-27T07:03:25.000000083+02:00[Europe/Paris]"]'     || [zonedDateTime]
        '["1900-06-13T12:29:03.000000431-04:00"]'                   || [offsetDateTime]
        '["19:37:00.000034576+07:00"]'                              || [offsetTime]
        '[{"special_type": "enum", "special_enum_class": ' +
                '"com.archiuse.mindis.json.JsonHelperSpec$TestEnum", ' +
                '"special_value": "SECOND"}]'                       || [TestEnum.SECOND]
    }

    void 'toObject converts json to object correctly'() {
        given: 'data object in json format'
        def pogoSrc = new TestPogo(prop1: 'prop1', prop2: Instant.now(), prop3: 42, prop4: 22.57G,
                prop5: [k1: 'v1', k2: 2], prop6: [1, 2, 3, 'a', 'b', 'c'])
        def json = JsonObject.mapFrom(pogoSrc)

        when: 'convert json to object'
        def actualPogo = jsonHelper.toObject(json, TestPogo)

        then: 'actual pogo is decoded correctly'
        actualPogo == pogoSrc
    }

    void 'fromJson bad special object throws JsonException'() {
        given: 'json with special object of unknown format'
        def badJson = new JsonObject('{"k1": {"special_type": "bad_type", "special_value": "bad_value"}}')

        when: 'decode json'
        jsonHelper.toMap(badJson, true)

        then:
        thrown JsonException
    }

    void 'fromJson special enum object with no enum class throws JsonException'() {
        given: 'json with special enum object that doesn\'t specify enum class'
        def badEnumJson = new JsonObject('{"k1": {"special_type": "enum", "special_value": "SECOND"}}')

        when: 'decode json to map'
        jsonHelper.toMap(badEnumJson, true)

        then:
        def e = thrown JsonException
        e.jsonObject
        e.jsonObject.toString().contains 'special_type'
        e.jsonObject.toString().contains 'enum'
    }

    @EqualsAndHashCode
    private static class TestPogo {
        String prop1
        Instant prop2
        Integer prop3
        BigDecimal prop4
        Map prop5
        List prop6
    }
}
