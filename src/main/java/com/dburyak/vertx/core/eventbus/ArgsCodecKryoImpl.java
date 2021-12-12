package com.dburyak.vertx.core.eventbus;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.MultiMap;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Secondary
@Setter(onMethod_ = {@Inject})
public class ArgsCodecKryoImpl implements ArgsCodec {
    private Kryo kryo;
    private Input input;
    private Output output;

    @Override
    public void encodeArgs(Object args, MultiMap headers) {
        if (args != null) {
            // TODO: currently here...........................................
            // use: ./hippo-auth/src/main/java/com/dburyak/hippo/auth/repository/impl/RefreshTokenValueRedisSerializer.java
            buffer.setByte(0, (byte) 0);
            messageCodec.encodeToWire(buffer, args);
        }
    }

    @Override
    public Object decodeArgs(MultiMap headers) {
        return null;
    }
}
