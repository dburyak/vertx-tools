package com.dburyak.vertx.core.call;

import com.dburyak.vertx.core.di.call.ForArgsCodec;
import io.micronaut.context.annotation.Secondary;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.nio.charset.Charset;

/**
 * Args codec that delegates all serialization and deserialization to main system {@link MessageCodec}.
 * So same strategy that is used for serializing and deserializing event bus messages is used also by this coded for
 * serializing/deserializing args.
 */
@Singleton
@Secondary
@Slf4j
public class MessageCodecDelegatingArgsCodecImpl implements ArgsCodec {
    private static final String HEADER_ARGS = "__args__";

    @Setter(onMethod_ = {@Inject})
    private MessageCodec<Object, Object> msgCodec;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@ForArgsCodec})
    private Charset argsCodecCharset;

    @Override
    public void encodeArgs(Object args, @NotNull MultiMap headers) {
        // extra caution step - remove anything that caller may have accidentally set in ARGS header
        if (headers.contains(HEADER_ARGS)) {
            if (log.isWarnEnabled()) {
                var headerData = headers.get(HEADER_ARGS);
                log.warn("reserved header name is used, header data is removed: header={}, value={}",
                        HEADER_ARGS, headerData);
            }
            headers.remove(HEADER_ARGS);
        }

        if (args != null) {
            var buf = Buffer.buffer();
            msgCodec.encodeToWire(buf, args);
            headers.add(HEADER_ARGS, buf.toString(argsCodecCharset));
        }
    }

    @Override
    public Object decodeArgs(@NotNull MultiMap headers) {
        var argsEncoded = headers.get(HEADER_ARGS);
        var buf = Buffer.buffer(argsEncoded, argsCodecCharset.name());
        return msgCodec.decodeFromWire(0, buf);
    }
}
