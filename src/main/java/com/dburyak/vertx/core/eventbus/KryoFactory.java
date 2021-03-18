package com.dburyak.vertx.core.eventbus;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;

import javax.inject.Singleton;

@Factory
@Secondary
public class KryoFactory {

    @Singleton
    @Secondary
    public Kryo kryo() {
        var kryo = new Kryo();
        kryo.setDefaultSerializer(BeanSerializer.class);
        return kryo;
    }
}
