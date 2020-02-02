package com.archiuse.mindis.test.integration

import com.archiuse.mindis.MindisVerticle
import com.archiuse.mindis.call.CallReceiverEBImpl
import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable

import javax.annotation.PostConstruct
import javax.inject.Inject

@Slf4j
class StackVerticle extends MindisVerticle {
    static final String ACTION_PUSH = 'push'
    static final String ACTION_POP = 'pop'
    static final String ACTION_SIZE = 'size'

    private Deque stack = new ArrayDeque()
    private CompositeDisposable actionRegistrations = new CompositeDisposable()

    @Inject
    CallReceiverEBImpl callReceiver

    @PostConstruct
    protected void init() {
        actions.addAll ACTION_PUSH, ACTION_POP
    }

    @Override
    protected Completable doStart() {
        Completable.merge([
                registerActionPush(),
                registerRequestPop(),
                registerRequestSize()
        ])
    }

    private Completable registerActionPush() {
        callReceiver
                .onCall(receiverName, ACTION_PUSH) { args, headers ->
                    log.debug 'pushing value to stack: val={}, suffix={}', args, headers?.suffix
                    if (headers?.suffix) {
                        stack.push args + headers.suffix
                    } else {
                        stack.push args
                    }
                }
                .doOnSuccess { actionRegistrations << it }
                .ignoreElement()
    }

    private Completable registerRequestPop() {
        callReceiver
                .onRequest(receiverName, ACTION_POP) { args, headers ->
                    Maybe.fromCallable {
                        def val = stack.pop()
                        log.debug 'popped value from stack: val={}', val
                        val
                    }
                }
                .doOnSuccess { actionRegistrations << it }
                .ignoreElement()
    }

    private Completable registerRequestSize() {
        callReceiver
                .onRequest(receiverName, ACTION_SIZE) { args, headers ->
                    Maybe.fromCallable {
                        def size = stack.size()
                        log.debug 'responding with stack size: size={}', size
                        size
                    }
                }
                .doOnSuccess { actionRegistrations << it }
                .ignoreElement()
    }
}
