package com.dburyak.vertx.eventbus.kryo.config;

import com.dburyak.vertx.eventbus.kryo.IdentifiableKryoSerializer;
import com.esotericsoftware.kryo.Kryo;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Singleton
@RequiredArgsConstructor
public class KryoSerializersConfigurer implements KryoConfigurer {
    private final List<IdentifiableKryoSerializer<?>> identifiableKryoSerializers;

    @Override
    public Kryo configure(Kryo kryo) {
        for (var reg : identifiableKryoSerializers) {
            kryo.register(reg.getType(), reg.getSerializer(), reg.getId());
        }
        return kryo;
    }
}
