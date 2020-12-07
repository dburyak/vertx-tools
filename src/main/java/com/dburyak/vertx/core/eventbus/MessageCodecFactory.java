package com.dburyak.vertx.core.eventbus;

import io.micronaut.context.annotation.Factory;
import io.vertx.core.eventbus.MessageCodec;

import javax.inject.Singleton;

@Factory
public class MessageCodecFactory {

    @Singleton
    public MessageCodec<Object, Object> messageCodec() {
        return new LocalAwareKryoUnsafeMessageCodec();
    }
}
