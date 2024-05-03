package com.dburyak.vertx.eventbus.kryo.config;

import com.esotericsoftware.kryo.Kryo;

/**
 * Configurer to allow customizing Kryo instance during bean initialization.
 * <p>
 * Kryo configurer is a function that takes a Kryo object and returns a modified Kryo instance. It can return both the
 * same object or a new object.
 */
public interface KryoConfigurer {
    Kryo configure(Kryo kryo);
}
