package com.archiuse.mindis

import com.archiuse.mindis.call.CallDispatcher
import com.archiuse.mindis.call.CallReceiver
import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable

import javax.inject.Inject

@Slf4j
class TestVerticle1 extends MindisVerticle {
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
                }
    }

    private void registerServices() {
        log.info 'registering health and ready for verticle1'
        log.debug 'callReceiver={}', this.callReceiver
        verticleServicesPublications << callReceiver.onRequest(receiverName, 'health') {
            Maybe.just([name: 'John Doe 1', age: 11, money: 15.15])
        }.subscribe()
        verticleServicesPublications << callReceiver.onRequest(receiverName, 'ready') {
            Maybe.just([name: 'Jane Doe 1', age: 12, money: 13.13])
        }.subscribe()
    }

    @Override
    protected Completable doStop() {
        Completable.fromAction {
            log.debug 'unregister verticle1 services'
            verticleServicesPublications.dispose()
        }
    }
}
