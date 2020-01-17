package com.archiuse.mindis.app

import com.archiuse.mindis.di.AppBean
import com.archiuse.mindis.di.EventLoop
import com.archiuse.mindis.di.Worker
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Context
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

import javax.annotation.PostConstruct
import javax.inject.Inject

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
        initBufferMetaClass()
        initCompositeDisposableMetaClass()
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
}
