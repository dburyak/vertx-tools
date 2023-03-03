package com.dburyak.vertx.eventbus.kryo;

import com.dburyak.vertx.core.di.VertxThreadScope;
import com.dburyak.vertx.eventbus.kryo.config.KryoCodecProperties;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Factory
@Requires(classes = Kryo.class)
public class KryoFactory {

    @Bean
    @VertxThreadScope
    @Requires(missingBeans = Kryo.class)
    public Kryo kryo(SerializerFactory<?> defaultKryoSerializerFactory) {
        var kryo = new Kryo();
        kryo.setDefaultSerializer(defaultKryoSerializerFactory);
        kryo.setRegistrationRequired(false);
        return kryo;
    }

    @Bean(preDestroy = "close")
    @VertxThreadScope
    @Requires(missingBeans = Input.class)
    public Input input() {
        return new Input();
    }

    @Bean(preDestroy = "close")
    @VertxThreadScope
    @Requires(missingBeans = Output.class)
    public Output output(KryoCodecProperties kryoProps) {
        return new Output(kryoProps.getOutputBufferInitialSize(), -1);
    }

    @Bean
    @VertxThreadScope
    @Requires(missingBeans = SerializerFactory.class)
    public SerializerFactory<?> defaultKryoSerializerFactory() {
        return new SerializerFactory.FieldSerializerFactory();
    }
}
