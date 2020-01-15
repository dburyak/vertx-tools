package com.archiuse.mindis.call

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable

/**
 * Inter-verticle calls receiver.
 * Allows to register call receiver in the system so other actors can call it.
 */
interface CallReceiver {

    /**
     * Register verticle service of type {@link ServiceType#CALL} with provided call handler.
     * @param rcv receiver name
     * @param action action name
     * @param doOnCall call handler, closure parameters: [call args, call headers map]
     * @return disposable that allows to unregister service
     */
    Single<Disposable> onCall(String rcv, String action, @ClosureParams(value = SimpleType,
            options = ['java.lang.Object', 'java.util.Map<java.lang.String, java.util.List>']) Closure<Void> doOnCall)

    /**
     * Register verticle service of type {@link ServiceType#REQUEST_RESPONSE} with provided request handler.
     * @param rcv receiver name
     * @param action action name
     * @param doOnRequest request handler, closure parameters: [request args, request headers map]
     * @return disposable that allows to unregister service
     */
    def <R> Single<Disposable> onRequest(String rcv, String action, @ClosureParams(value = SimpleType,
            options = ['java.lang.Object', 'java.util.Map<java.lang.String, java.util.List>'])
            Closure<Maybe<R>> doOnRequest)

    /**
     * Subscribe to verticle service of type {@link ServiceType#PUB_SUB_TOPIC} with provided event handler.
     * @param rcv receiver name
     * @param action action name
     * @param doOnEvent event handler, closure parameters: [event args, event headers map]
     * @return disposable that allows to unsubscribe this subscription to topic
     */
    Single<Disposable> subscribe(String rcv, String action, @ClosureParams(value = SimpleType,
            options = ['java.lang.Object', 'java.util.Map<java.lang.String, java.util.List>'])
            Closure<Void> doOnEvent)
}
