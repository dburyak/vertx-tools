package com.dburyak.vertx.eventbus.kryo;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.eventbus.MessageCodec;

import javax.inject.Singleton;

@Factory
@Secondary
public class MessageCodecFactory {

    @Singleton
    @Secondary
    public MessageCodec<Object, Object> messageCodec() {
        return new LocalAwareKryoUnsafeMessageCodec();
    }
}
