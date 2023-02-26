package com.dburyak.vertx.eventbus;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

import java.util.List;

public abstract class JsonMessageCodec<S, R> implements MessageCodec<S, R> {

    @Override
    public final void encodeToWire(Buffer buffer, S data) {
        var className = data.getClass().getCanonicalName();
        var jsonStr = Json.encode(data);
        buffer.appendInt(className.length());
        buffer.appendString(className);
        buffer.appendInt(jsonStr.length());
        buffer.appendString(jsonStr);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final R decodeFromWire(int pos, Buffer buffer) {
        var decodedObjects = readMsgObjectsFromWire(pos, buffer);
        var dataClass = (Class<R>) decodedObjects.get(0);
        String jsonStr = (String) decodedObjects.get(1);
        return Json.decodeValue(jsonStr, dataClass);
    }

    @Override
    public final String name() {
        return getClass().getCanonicalName();
    }

    @Override
    public final byte systemCodecID() {
        return -1;
    }

    private List<Object> readMsgObjectsFromWire(int pos, Buffer buffer) {
        var p = pos;
        var classNameSize = buffer.getInt(p);
        p += Integer.BYTES;
        var className = buffer.getString(p, p + classNameSize);
        p += classNameSize;
        var jsonStrSize = buffer.getInt(p);
        p += Integer.BYTES;
        var jsonStr = buffer.getString(p, p + jsonStrSize);
        try {
            return List.of(Class.forName(className), jsonStr);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("unknown type of encoded data: " + className, e);
        }
    }
}
