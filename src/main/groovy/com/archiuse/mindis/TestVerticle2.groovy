package com.archiuse.mindis

import com.archiuse.mindis.call.CallDispatcher
import com.archiuse.mindis.call.CallReceiver
import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable

import javax.inject.Inject

import static java.util.concurrent.TimeUnit.SECONDS

@Slf4j
class TestVerticle2 extends MindisVerticle {
    private CompositeDisposable verticleServicesPublications = new CompositeDisposable()

    @Inject
    CallReceiver callReceiver

    @Inject
    CallDispatcher callDispatcher

    @Override
    protected Completable doStart() {
        Completable
                .fromAction {
                    registerServices()
                    runTestCallsLater()
                }
    }

    private void registerServices() {
        log.info 'registering health and ready for verticle2'
        log.debug 'callReceiver={}', this.callReceiver
        verticleServicesPublications << callReceiver.onRequest(receiverName, 'health') {
            Maybe.just([name: 'John Doe 2', age: 25, money: 22.25])
        }.subscribe()
        verticleServicesPublications << callReceiver.onRequest(receiverName, 'ready') {
            Maybe.just([name: 'Jane Doe 2', age: 26, money: 23.23])
        }.subscribe()
    }

    private void runTestCallsLater() {
        Completable.timer(3, SECONDS)
                .doOnComplete { log.debug 'calling verticle1 from verticle2' }
                .andThen(callDispatcher.request(TestVerticle1.class.canonicalName, 'health'))
                .delay(3, SECONDS)
                .concatWith(callDispatcher.request(TestVerticle1.class.canonicalName, 'ready'))
                .subscribe({
                    log.debug 'verticle1 action request from verticle2 result: {}', it
                }, {
                    log.error 'verticle1 action request from verticle2 failed', it
                })
    }

    @Override
    protected Completable doStop() {
        Completable.fromAction {
            log.debug 'unregister verticle2 services'
            verticleServicesPublications.dispose()
        }
    }
}
