package com.archiuse.mindis.call


import com.archiuse.mindis.json.JsonHelper
import groovy.util.logging.Slf4j
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

import javax.inject.Inject
import javax.inject.Singleton

@Slf4j
@Singleton
class LocalEBAwareJsonMessageCodec implements MessageCodec {
    private static final String CODEC_NAME = LocalEBAwareJsonMessageCodec.canonicalName

    @Inject
    JsonHelper jsonHelper

    @Override
    void encodeToWire(Buffer buffer, Object data) {
        def className = data.getClass().canonicalName
        def jsonStr = jsonHelper.toJson(data).encode()
        buffer << className.size()
        buffer << className
        buffer << jsonStr.size()
        buffer << jsonStr
    }

    @Override
    Object decodeFromWire(int pos, Buffer buffer) {
        def (Class type, String jsonStr) = readMsgObjectsFromWire(pos, buffer)
        if (JsonObject.isAssignableFrom(type)) {
            new JsonObject(jsonStr)
        } else if (JsonArray.isAssignableFrom(type)) {
            new JsonArray(jsonStr)
        } else if (Map.isAssignableFrom(type)) {
            jsonHelper.toMap new JsonObject(jsonStr)
        } else if (Iterable.isAssignableFrom(type)) {
            jsonHelper.toList new JsonArray(jsonStr)
        } else {
            jsonHelper.toObject(jsonStr as JsonObject, type)
        }
    }

    @Override
    Object transform(Object data) {
        data
    }

    @Override
    String name() {
        CODEC_NAME
    }

    @Override
    byte systemCodecID() {
        -1
    }

    private def readMsgObjectsFromWire(int pos, Buffer buffer) {
        def p = pos
        def classNameSize = buffer.getInt(p)
        p += Integer.BYTES

        def className = buffer.getString(p, p + classNameSize)
        p += classNameSize

        def jsonSize = buffer.getInt(p)
        p += Integer.BYTES

        def jsonStr = buffer.getString(p, jsonSize)

        [className as Class, jsonStr]
    }
}
