package com.dburyak.vertx.core.eventbus;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.Setter;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

@Setter(onMethod_ = {@Inject})
public abstract class KryoMessageCodec implements MessageCodec<Object, Object> {
    protected Kryo kryo;

    @Override
    public final void encodeToWire(Buffer buffer, Object data) {
        var buf = new ByteArrayOutputStream();
        try (var out = new Output(buf)) {
            kryo.writeClassAndObject(out, data);
        }
        buffer.appendBytes(buf.toByteArray());
    }

    @Override
    public final Object decodeFromWire(int pos, Buffer buffer) {
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
