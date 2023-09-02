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

/**
 * Factory for Kryo related default implementations beans.
 */
@Factory
@Requires(classes = Kryo.class)
public class KryoFactory {

    /**
     * Kryo default bean. This is not thread safe object, so we're using vertx thread scope to make sure that each vertx
     * thread has its own instance of Kryo.
     *
     * @param defaultKryoSerializerFactory default serializer factory
     *
     * @return Kryo instance
     */
    @Bean
    @VertxThreadScope
    @Requires(missingBeans = Kryo.class)
    public Kryo kryo(SerializerFactory<?> defaultKryoSerializerFactory) {
        var kryo = new Kryo();
        kryo.setDefaultSerializer(defaultKryoSerializerFactory);
        kryo.setRegistrationRequired(false);
        return kryo;
    }

    /**
     * Kryo Input default bean. This is not thread safe object, so we're using vertx thread scope to make sure that each
     * vertx thread has its own instance of Kryo Input.
     *
     * @return Kryo Input instance
     */
    @Bean(preDestroy = "close")
    @VertxThreadScope
    @Requires(missingBeans = Input.class)
    public Input input() {
        return new Input();
    }

    /**
     * Kryo Output default bean. This is not thread safe object, so we're using vertx thread scope to make sure that
     * each vertx thread has its own instance of Kryo Output.
     *
     * @param kryoProps kryo properties
     *
     * @return Kryo Output instance
     */
    @Bean(preDestroy = "close")
    @VertxThreadScope
    @Requires(missingBeans = Output.class)
    public Output output(KryoCodecProperties kryoProps) {
        return new Output(kryoProps.getOutputBufferInitialSize(), -1);
    }

    /**
     * Kryo SerializerFactory default bean.
     *
     * @return Kryo SerializerFactory instance
     */
    @Bean
    @VertxThreadScope
    @Requires(missingBeans = SerializerFactory.class)
    public SerializerFactory<?> defaultKryoSerializerFactory() {
        return new SerializerFactory.FieldSerializerFactory();
    }
}
