package com.dburyak.vertx.eventbus.kryo;

import com.esotericsoftware.kryo.Serializer;

/**
 * Kryo serializer with an ID to unambiguously identify it. Fixed IDs are needed to make sure that the same serializer
 * is used on both sides of the event bus.
 */
public interface IdentifiableKryoSerializer<T> {

    /**
     * Get unique ID of the associated serializer.
     *
     * @return unique ID of the associated serializer
     */
    int getId();


    /**
     * Get the type this serializer can serialize.
     *
     * @return class of the objects that this serializer can serialize
     */
    Class<T> getType();

    /**
     * Get serializer instance.
     *
     * @return serializer instance
     */
    Serializer<T> getSerializer();
}
