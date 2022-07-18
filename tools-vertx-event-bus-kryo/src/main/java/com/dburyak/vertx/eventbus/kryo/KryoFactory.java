package com.dburyak.vertx.eventbus.kryo;

import com.dburyak.vertx.core.di.VertxThreadScope;
import com.dburyak.vertx.eventbus.kryo.config.KryoCodecProperties;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;

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

    @Bean(preDestroy = "close")
    @VertxThreadScope
    @Secondary
    public Input input() {
        return new Input();
    }

    @Bean(preDestroy = "close")
    @VertxThreadScope
    @Secondary
    public Output output(KryoCodecProperties kryoProps) {
        return new Output(kryoProps.getOutputBufferInitialSize(), -1);
    }

    @Bean
    @VertxThreadScope
    @Secondary
    public SerializerFactory<?> defaultKryoSerializerFactory() {
        var config = new CompatibleFieldSerializer.CompatibleFieldSerializerConfig();
        return new SerializerFactory.CompatibleFieldSerializerFactory(config);
    }
}
