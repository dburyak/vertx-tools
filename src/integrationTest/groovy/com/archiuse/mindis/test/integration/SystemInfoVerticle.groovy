package com.archiuse.mindis.test.integration

import com.archiuse.mindis.MindisVerticle
import com.archiuse.mindis.VerticleProducer
import com.archiuse.mindis.call.CallReceiver
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable

import javax.annotation.PostConstruct
import javax.inject.Inject
import java.time.Duration
import java.time.Instant

import static java.lang.Runtime.runtime
import static java.time.Instant.now

class SystemInfoVerticle extends MindisVerticle {
    static final String ACTION_MEMORY = 'memory'
    static final String ACTION_TIME = 'system_time'
    static final String ACTION_UPTIME = 'uptime'
    static final String ACTION_ECHO = 'echo'
    static final String ACTION_STATIC_DATA = 'request'
    static final String STATIC_DATA_RESPONSE = 'response'

    private Disposable memoryHandlerReg
    private Disposable systimeHandlerReg
    private Disposable uptimeHandlerReg
    private Disposable echoHandlerReg
    private Disposable staticDataHandlerReg
    private Instant startedAt

    @Inject
    CallReceiver callReceiver

    static VerticleProducer getProducer() {
        new Producer()
    }

    @PostConstruct
    void init() {
        actions.addAll ACTION_MEMORY, ACTION_TIME, ACTION_MEMORY
    }

    @Override
    protected Completable doStart() {
        Completable.merge([
                Completable.fromAction { startedAt = now() },
                registerMemoryReqHandler(),
                registerSystimeReqHandler(),
                registerUptimeReqHandler(),
                registerEchoReqHandler()
        ])
    }

    private Completable registerMemoryReqHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_MEMORY) {
                    Maybe.fromCallable { memStats }
                }
                .doOnSuccess { memoryHandlerReg = it }
                .ignoreElement()
    }

    private Completable registerSystimeReqHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_TIME) {
                    Maybe.fromCallable { systemTime }
                }
                .doOnSuccess { systimeHandlerReg = it }
                .ignoreElement()
    }

    private Completable registerUptimeReqHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_UPTIME) {
                    Maybe.fromCallable { uptime }
                }
                .doOnSuccess { uptimeHandlerReg = it }
                .ignoreElement()
    }

    private Completable registerEchoReqHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_ECHO) { args, headers ->
                    Maybe.fromCallable { args }
                }
                .doOnSuccess { echoHandlerReg = it }
                .ignoreElement()
    }

    private Completable registerStaticDataHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_STATIC_DATA) {
                    Maybe.fromCallable { STATIC_DATA_RESPONSE }
                }
                .doOnSuccess { staticDataHandlerReg = it }
                .ignoreElement()
    }

    private MemStats getMemStats() {
        def allocated = runtime.totalMemory()
        def free = runtime.freeMemory()
        new MemStats(max: runtime.maxMemory(), allocated: allocated, free: free, used: allocated - free)
    }

    private Instant getSystemTime() {
        now()
    }

    private Duration getUptime() {
        Duration.between(startedAt, now())
    }

    private static class Producer extends VerticleProducer {
        String name = 'TestVerticleProducer'

        @Override
        MindisVerticle doCreateVerticle() {
            new SystemInfoVerticle().tap {
                name = 'TestVerticle'
            }
        }
    }
}
