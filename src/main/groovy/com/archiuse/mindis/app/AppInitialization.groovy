package com.archiuse.mindis.app

import com.archiuse.mindis.di.AppBean
import com.archiuse.mindis.di.EventLoop
import com.archiuse.mindis.di.Worker
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Context
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

import javax.annotation.PostConstruct
import javax.inject.Inject
import java.time.Duration

import static java.util.concurrent.TimeUnit.DAYS
import static java.util.concurrent.TimeUnit.HOURS
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.MINUTES
import static java.util.concurrent.TimeUnit.NANOSECONDS
import static java.util.concurrent.TimeUnit.SECONDS

@Context
@AppBean
@Slf4j
class AppInitialization {

    @Inject
    @EventLoop
    Scheduler vertxRxScheduler

    @Inject
    @Worker
    Scheduler vertxRxBlockingScheduler

    @PostConstruct
    void init() {
        log.info 'initializing application'
        registerRxJavaVertxSchedulers()
        initStringMetaClass()
        initMapMetaClass()
        initBufferMetaClass()
        initCompositeDisposableMetaClass()
        initRxMetaClasses()
        log.info 'application initialized'
    }

    private void registerRxJavaVertxSchedulers() {
        log.debug 'register vertx rx schedulers'
        RxJavaPlugins.computationSchedulerHandler = { vertxRxScheduler }
        RxJavaPlugins.ioSchedulerHandler = { vertxRxBlockingScheduler }
        RxJavaPlugins.newThreadSchedulerHandler = { vertxRxScheduler }
    }

    private void initBufferMetaClass() {
        log.debug 'init MetaClass: {}', Buffer
        Buffer.metaClass.leftShift = { JsonObject json ->
            json.writeToBuffer(delegate)
        }
        Buffer.metaClass.leftShift = { JsonArray jsonArray ->
            jsonArray.writeToBuffer(delegate)
        }
        Buffer.metaClass.leftShift = { String str ->
            delegate.appendString(str)
        }
        Buffer.metaClass.leftShift = { Integer intVal ->
            delegate.appendInt(intVal)
        }
    }

    private void initStringMetaClass() {
        log.debug 'init MetaClass: {}', String
        def defaultAsType = String.metaClass.getMetaMethod('asType', [Class] as Class[])
        String.metaClass.asType = { Class type ->
            if (JsonObject.isAssignableFrom(type)) {
                new JsonObject(delegate)
            } else if (JsonArray.isAssignableFrom(type)) {
                new JsonArray(delegate)
            } else {
                defaultAsType.invoke(delegate, type)
            }
        }
    }

    private void initCompositeDisposableMetaClass() {
        log.debug 'init MetaClass: {}', CompositeDisposable
        CompositeDisposable.metaClass.leftShift = { Disposable disposable ->
            delegate.add(disposable)
            delegate
        }
        CompositeDisposable.metaClass.leftShift = { Iterable<Disposable> disposables ->
            delegate.addAll(disposables)
            delegate
        }
    }

    private void initMapMetaClass() {
        log.debug 'init MetaClass: {}', Map
        def defaultAsType = Map.metaClass.getMetaMethod('asType', [Class] as Class[])
        Map.metaClass.asType = { Class type ->
            if (JsonObject.isAssignableFrom(type)) {
                new JsonObject(delegate)
            } else if (DeliveryOptions.isAssignableFrom(type)) {
                new DeliveryOptions(new JsonObject(delegate))
            } else {
                defaultAsType.invoke(delegate, type)
            }
        }
    }

    private void initRxMetaClasses() {
        log.debug 'init MetaClass: RxClasses'
        Single.metaClass.delay = rxDelayWithDuration
        Completable.metaClass.delay = rxDelayWithDuration
        Observable.metaClass.delay = rxDelayWithDuration
        Flowable.metaClass.delay = rxDelayWithDuration
        Maybe.metaClass.delay = rxDelayWithDuration
    }

    private Closure getRxDelayWithDuration() {
        return { Duration d ->
            if (d.zero) {
                return delegate
            }
            if (d.negative) {
                throw new IllegalArgumentException('can\'t have a delay with negative duration')
            }
            def nPart = d.toNanosPart()
            def toMillis = d.toMillis()
            def nanosDiv = nPart.intdiv 1000000
            def nanosMod = nPart % 1000000
            def isNanosOnly = nanosMod && (nanosDiv as Long == toMillis)
            if (isNanosOnly) {
                delegate.delay(d.toNanos(), NANOSECONDS)
            } else if (d.toMillisPart()) {
                delegate.delay(d.toMillis(), MILLISECONDS)
            } else if (d.toSecondsPart()) {
                delegate.delay(d.toSeconds(), SECONDS)
            } else if (d.toMinutesPart()) {
                delegate.delay(d.toMinutes(), MINUTES)
            } else if (d.toHoursPart()) {
                delegate.delay(d.toHours(), HOURS)
            } else if (d.toDays()) {
                delegate.delay(d.toDays(), DAYS)
            }
        }
    }
}
