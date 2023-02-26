package com.dburyak.vertx.eventbus;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Event bus message codec with custom name. Allows to reuse same codec instance (singleton bean) with different codec
 * names.
 *
 * @param <S> send type
 * @param <R> receive type
 */
@AllArgsConstructor
@ToString
public class NamedMessageCodec<S, R> implements MessageCodec<S, R> {
    private final String name;
    private final MessageCodec<S, R> codec;

    public static <S, R> NamedMessageCodec<S, R> of(String name, MessageCodec<S, R> codec) {
        return new NamedMessageCodec<>(name, codec);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void encodeToWire(Buffer buffer, S s) {
        codec.encodeToWire(buffer, s);
    }

    @Override
    public R decodeFromWire(int pos, Buffer buffer) {
        return codec.decodeFromWire(pos, buffer);
    }

    @Override
    public R transform(S s) {
        return codec.transform(s);
    }

    @Override
    public byte systemCodecID() {
        return codec.systemCodecID();
    }
}
