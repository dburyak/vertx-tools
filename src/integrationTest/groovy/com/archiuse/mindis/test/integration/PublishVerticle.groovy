package com.archiuse.mindis.test.integration

import com.archiuse.mindis.MindisVerticle
import com.archiuse.mindis.call.CallDispatcherEBImpl
import groovy.util.logging.Slf4j
import io.reactivex.Completable

import javax.annotation.PostConstruct
import javax.inject.Inject

@Slf4j
class PublishVerticle extends MindisVerticle {
    static final String ACTION_PUBLISH = 'publish'

    @Inject
    CallDispatcherEBImpl callDispatcher

    @PostConstruct
    protected void init() {
        actions << ACTION_PUBLISH
    }

    @Override
    protected Completable doStart() {
        Completable.merge([

        ])
    }

    private Completable registerActionPublish() {
    }
}
