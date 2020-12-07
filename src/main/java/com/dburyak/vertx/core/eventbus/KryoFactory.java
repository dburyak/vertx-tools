package com.dburyak.vertx.core.eventbus;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class KryoFactory {

    @Singleton
    public Kryo kryo() {
        var kryo = new Kryo();
        kryo.setDefaultSerializer(BeanSerializer.class);
        return kryo;
    }
}
