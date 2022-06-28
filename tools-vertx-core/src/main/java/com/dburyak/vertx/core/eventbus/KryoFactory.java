package com.dburyak.vertx.core.eventbus;

import com.dburyak.vertx.core.di.VerticleScope;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Value;

import javax.inject.Singleton;

@Factory
@Secondary
public class KryoFactory {

    @Singleton
    @VerticleScope
    @Secondary
    public Kryo kryo(SerializerFactory<?> defaultKryoSerializerFactory) {
        var kryo = new Kryo();
        kryo.setDefaultSerializer(defaultKryoSerializerFactory);
        kryo.setRegistrationRequired(false);
        return kryo;
    }

    @Singleton
    @Bean(preDestroy = "close")
    @VerticleScope
    @Secondary
    public Input input() {
        return new Input();
    }

    @Singleton
    @Bean(preDestroy = "close")
    @VerticleScope
    @Secondary
    public Output output(@Value("${tools.eventbus.kryo.output.buffer.initial-size:1024}")
            int initialKryoOutputBufferSize) {
        return new Output(initialKryoOutputBufferSize, -1);
    }

    @Singleton
    @Secondary
    public SerializerFactory<?> defaultKryoSerializerFactory() {
        var config = new CompatibleFieldSerializer.CompatibleFieldSerializerConfig();
        return new SerializerFactory.CompatibleFieldSerializerFactory(config);
    }
}
