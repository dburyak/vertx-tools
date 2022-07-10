package com.dburyak.vertx.eventbus.kryo;

import com.dburyak.vertx.core.di.VerticleScope;
import com.dburyak.vertx.core.di.VertxThreadScope;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Factory
@Requires(classes = Kryo.class)
@Secondary
public class KryoFactory {

    @Bean
    @VertxThreadScope
    @Secondary
    public Kryo kryo(SerializerFactory<?> defaultKryoSerializerFactory) {
        var kryo = new Kryo();
        kryo.setDefaultSerializer(defaultKryoSerializerFactory);
        kryo.setRegistrationRequired(false);
        return kryo;
    }

    @Singleton
    @Bean(preDestroy = "close")
    @VertxThreadScope
    @Secondary
    public Input input() {
        return new Input();
    }

    @Singleton
    @Bean(preDestroy = "close")
    @VertxThreadScope
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
