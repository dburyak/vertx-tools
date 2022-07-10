package com.dburyak.vertx.eventbus.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.ByteArrayOutputStream;

public abstract class KryoMessageCodecBase implements MessageCodec<Object, Object> {
    protected Kryo kryo;

    @Override
    public final void encodeToWire(Buffer buffer, Object data) {
        var buf = new ByteArrayOutputStream();
        // TODO: output is an expensive resource and should be shared and reused, allocated on init
        try (var out = new Output(buf)) {
            kryo.writeClassAndObject(out, data);
        }
        buffer.appendBytes(buf.toByteArray());
    }

    @Override
    public final Object decodeFromWire(int pos, Buffer buffer) {
        // TODO: input is an expensive resource and should be shared and reused, allocated on init
        try (var in = new Input(buffer.getBytes(pos, buffer.length()))) {
            return kryo.readClassAndObject(in);
        }
    }

    @Override
    public final String name() {
        return getClass().getCanonicalName();
    }

    @Override
    public final byte systemCodecID() {
        return -1;
    }
}
