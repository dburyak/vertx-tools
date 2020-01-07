package com.archiuse.mindis.call

import io.reactivex.Observable
import io.reactivex.Single

interface CallReceiver {
    // TODO: add "registerCallHandlerXXX" methods here one for each "CallDispatcher" call type

    void onCall(String rcv, String action, Closure<Void> doOnCall)

    def <T> void onRequest(String rcv, String action, Closure<Single<T>> doOnRequest)

    def <T> Observable<T> subscribe(String rcv, String action, Closure<Void> doOnEvent)
}
