package com.dburyak.vertx.core.call;

import io.vertx.core.MultiMap;

import javax.validation.constraints.NotNull;

public interface ArgsCodec {
    void encodeArgs(Object args, @NotNull MultiMap headers);

    Object decodeArgs(@NotNull MultiMap headers);
}
