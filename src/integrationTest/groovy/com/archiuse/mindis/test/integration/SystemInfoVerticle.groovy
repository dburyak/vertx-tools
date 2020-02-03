package com.archiuse.mindis.test.integration

import com.archiuse.mindis.MindisVerticle
import com.archiuse.mindis.VerticleProducer
import com.archiuse.mindis.call.CallDispatcher
import com.archiuse.mindis.call.CallReceiver
import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable

import javax.annotation.PostConstruct
import javax.inject.Inject
import java.time.Duration
import java.time.Instant

import static java.lang.Runtime.runtime
import static java.time.Instant.now

@Slf4j
class SystemInfoVerticle extends MindisVerticle {
    static final String ACTION_MEMORY = 'memory'
    static final String ACTION_TIME = 'system_time'
    static final String ACTION_UPTIME = 'uptime'
    static final String ACTION_CRITICAL_ERROR = 'publish_critical_error'
    static final String TOPIC_CRITICAL_ERROR = 'critical_error'

    private final CompositeDisposable reg = new CompositeDisposable()
    private Instant startedAt

    @Inject
    CallReceiver callReceiver

    @Inject
    CallDispatcher callDispatcher

    static VerticleProducer getProducer() {
        new Producer()
    }

    @PostConstruct
    void init() {
        actions.addAll ACTION_MEMORY, ACTION_TIME, ACTION_MEMORY, ACTION_CRITICAL_ERROR
    }

    @Override
    protected Completable doStart() {
        Completable.merge([
                Completable.fromAction { startedAt = now() },
                registerMemoryReqHandler(),
                registerSystimeReqHandler(),
                registerUptimeReqHandler(),
                registerCriticalErrorCallHandler()
        ])
    }

    private Completable registerMemoryReqHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_MEMORY) {
                    Maybe.fromCallable { memStats }
                }
                .doOnSuccess { reg << it }
                .ignoreElement()
    }

    private Completable registerSystimeReqHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_TIME) {
                    Maybe.fromCallable { systemTime }
                }
                .doOnSuccess { reg << it }
                .ignoreElement()
    }

    private Completable registerUptimeReqHandler() {
        callReceiver
                .onRequest(receiverName, ACTION_UPTIME) {
                    Maybe.fromCallable { uptime }
                }
                .doOnSuccess { reg << it }
                .ignoreElement()
    }

    private Completable registerCriticalErrorCallHandler() {
        callReceiver
                .onCall(receiverName, ACTION_CRITICAL_ERROR) { msg, headers ->
                    def pubMsg = headers?.suffix ? msg + headers.suffix : msg
                    callDispatcher.publish(receiverName, TOPIC_CRITICAL_ERROR, pubMsg, headers).subscribe()
                }
                .doOnSuccess { reg << it }
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
