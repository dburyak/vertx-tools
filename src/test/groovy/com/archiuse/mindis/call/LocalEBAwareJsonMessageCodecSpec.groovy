package com.archiuse.mindis.call

import com.archiuse.mindis.json.JsonHelper
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class LocalEBAwareJsonMessageCodecSpec extends Specification {
    LocalEBAwareJsonMessageCodec localEBAwareJsonMessageCodec = new LocalEBAwareJsonMessageCodec()
    JsonHelper jsonHelper = Mock(JsonHelper)

    def stringDefaultMetaClass

    @Shared
    def pogoMap = [prop1: 'str', prop2: 42, prop3: Instant.ofEpochSecond(1567), prop4: 123.456G]

    void setup() {
        stringDefaultMetaClass = String.metaClass
        def stringDefaultAsType = String.metaClass.getMetaMethod('asType', [Class] as Class[])
        String.metaClass.asType = { Class type ->
            if (JsonObject.isAssignableFrom(type)) {
                new JsonObject(delegate)
            } else if (JsonArray.isAssignableFrom(type)) {
                new JsonArray(delegate)
            } else {
                stringDefaultAsType.invoke(delegate, type)
            }
        }
        localEBAwareJsonMessageCodec.jsonHelper = jsonHelper
    }

    void cleanup() {
        String.metaClass = stringDefaultMetaClass
    }

    def 'encodeToWire encodes correctly'() {
        given: 'test object to encode'
        def ts = Instant.ofEpochSecond(157)
        def objPropsMap = [prop1: 'str', prop2: 42, prop3: ts, prop4: 123.456G]
        def obj = new TestPogo(objPropsMap)
        def className = obj.getClass().canonicalName
        def json = new JsonObject(objPropsMap)
        def jsonStr = json.encode()
        def buf = Mock(Buffer)
        Buffer.metaClass.leftShift = { String str ->
            delegate.appendString(str)
        }
        Buffer.metaClass.leftShift = { Integer intVal ->
            delegate.appendInt(intVal)
        }

        when: 'encode object'
        localEBAwareJsonMessageCodec.encodeToWire(buf, obj)

        then:
        noExceptionThrown()

        then: 'jsonHelper is used to encode object POGO'
        1 * jsonHelper.toJson(obj) >> json

        then: 'className size is put first'
        1 * buf.appendInt(className.size())

        then: 'className string is put next'
        1 * buf.appendString(className)

        then: 'json string payload size is put next'
        1 * buf.appendInt(jsonStr.size())

        then: 'json string payload is put next'
        1 * buf.appendString(jsonStr)
    }

    @Unroll
    def 'decodeFromWire decodes correctly'() {
        given: 'test object to encode'
        def buf = Mock(Buffer)
        def pos = 100
        def pos2 = pos + Integer.BYTES
        def pos3 = pos2 + className.size()
        def pos4 = pos3 + Integer.BYTES
        def pos5 = pos4 + jsonStr.size()

        def jsonObj = Json.decodeValue(jsonStr) instanceof JsonObject ? new JsonObject(jsonStr).map : null
        def jsonArray = Json.decodeValue(jsonStr) instanceof JsonArray ? new JsonArray(jsonStr).list : null

        when: 'decode buffer'
        def res = localEBAwareJsonMessageCodec.decodeFromWire(pos, buf)

        then:
        noExceptionThrown()
        (className as Class).isAssignableFrom(res.getClass())
        res == expectedResult

        and:
        1 * buf.getInt(pos) >> className.size()
        1 * buf.getString(pos2, pos3) >> className
        1 * buf.getInt(pos3) >> jsonStr.size()
        1 * buf.getString(pos4, pos5) >> jsonStr

        and:
        (_..1) * jsonHelper.toObject({ it.toString() == new JsonObject(pogoMap).toString() }, TestPogo)
                >> new TestPogo(pogoMap)
        (_..1) * jsonHelper.toMap(Json.decodeValue(jsonStr)) >> jsonObj
        (_..1) * jsonHelper.toList(Json.decodeValue(jsonStr)) >> jsonArray

        where:
        className                | jsonStr                                          || expectedResult
        JsonObject.canonicalName | new JsonObject(prop1: 'str', prop2: 42).encode() || new JsonObject(jsonStr)
        JsonArray.canonicalName  | new JsonArray(['str', 42, 123.456G]).encode()    || new JsonArray(jsonStr)
        Map.canonicalName        | new JsonObject(prop3: 3, prop4: 1.4G).encode()   || new JsonObject(jsonStr).map
        List.canonicalName       | new JsonArray([1, 'hello', 123.4535G]).encode()  || new JsonArray(jsonStr).list
        TestPogo.canonicalName   | new JsonObject(pogoMap).encode()                 || new TestPogo(pogoMap)
    }
}
