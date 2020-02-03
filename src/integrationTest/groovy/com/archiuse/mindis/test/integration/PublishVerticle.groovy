package com.archiuse.mindis.test.integration

import com.archiuse.mindis.MindisVerticle
import com.archiuse.mindis.call.CallReceiverEBImpl
import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.reactivex.Maybe

import javax.annotation.PostConstruct
import javax.inject.Inject

@Slf4j
class PublishVerticle extends MindisVerticle {
    static final String ACTION_PUBLISH = 'publish'

    @Inject
    CallReceiverEBImpl callReceiver

    @PostConstruct
    protected void init() {
        actions << ACTION_PUBLISH
    }

    @Override
    protected Completable doStart() {
        Completable.merge([

        ])
    }

    private Completable registerPublishHandler() {
        callReceiver
                .subscribe(receiverName, ACTION_STATIC_DATA) {
                    Maybe.fromCallable { STATIC_DATA_RESPONSE }
                }
                .doOnSuccess { staticDataHandlerReg = it }
                .ignoreElement()
    }
}
