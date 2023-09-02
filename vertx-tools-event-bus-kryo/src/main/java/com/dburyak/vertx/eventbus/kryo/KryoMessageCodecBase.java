package com.dburyak.vertx.eventbus.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.micronaut.context.ApplicationContext;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base class for Kryo message codecs. It provides thread-safe access to Kryo, Input and Output objects, and caches them
 * properly.
 *
 * @param <S> type of message to send
 * @param <R> type of message to receive
 */
public abstract class KryoMessageCodecBase<S, R> implements MessageCodec<S, R> {
    private ApplicationContext appCtx;
    private final ConcurrentMap<String, Kryo> kryos = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Input> inputs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Output> outputs = new ConcurrentHashMap<>();

    @Override
    public final void encodeToWire(Buffer buffer, S data) {
        var kryo = getKryo();
        var output = getOutput();
        output.reset();
        kryo.writeClassAndObject(output, data);
        buffer.appendBytes(output.getBuffer(), 0, output.position());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final R decodeFromWire(int pos, Buffer buffer) {
        var kryo = getKryo();
        var input = getInput();
        input.setBuffer(buffer.getBytes(pos, buffer.length()));
        return (R) kryo.readClassAndObject(input);
    }

    @Override
    public final String name() {
        return getClass().getCanonicalName();
    }

    @Override
    public final byte systemCodecID() {
        return -1;
    }

    /**
     * Set application context.
     *
     * @param applicationContext application context
     */
    @Inject
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.appCtx = applicationContext;
    }

    /**
     * Get Kryo instance for current thread. Creates new instance if it doesn't exist yet.
     *
     * @return Kryo instance
     */
    protected final Kryo getKryo() {
        return kryos.computeIfAbsent(Thread.currentThread().getName(), threadName -> appCtx.getBean(Kryo.class));
    }

    /**
     * Get Kryo Input instance for current thread. Creates new instance if it doesn't exist yet.
     *
     * @return Kryo Input instance
     */
    protected final Input getInput() {
        return inputs.computeIfAbsent(Thread.currentThread().getName(), threadName -> appCtx.getBean(Input.class));
    }

    /**
     * Get Kryo Output instance for current thread. Creates new instance if it doesn't exist yet.
     *
     * @return Kryo Output instance
     */
    protected final Output getOutput() {
        return outputs.computeIfAbsent(Thread.currentThread().getName(), threadName -> appCtx.getBean(Output.class));
    }

    @PreDestroy
    void destroy() {
        inputs.forEach((threadName, input) -> input.close());
        outputs.forEach((threadName, output) -> output.close());
        kryos.clear();
        inputs.clear();
        outputs.clear();
    }
}
