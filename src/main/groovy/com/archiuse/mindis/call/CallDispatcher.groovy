package com.archiuse.mindis.call

import io.reactivex.Completable
import io.reactivex.Maybe
import io.vertx.core.eventbus.DeliveryOptions

/**
 * Inter-verticle call dispatcher.
 * Allows to route calls to other actors in the system.
 */
interface CallDispatcher {

    /**
     * Point-to-point actor action call.
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     * @param rcv receiver id
     * @param action action id
     * @return call status
     */
    Completable call(String rcv, String action)

    /**
     * Point-to-point actor action call.
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     * @param rcv receiver id
     * @param action action id
     * @param opts call delivery options
     * @return call status
     */
    Completable call(String rcv, String action, DeliveryOptions opts)

    /**
     * Point-to-point actor action call with arguments.
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     * @param rcv receiver id
     * @param action action id
     * @param args call arguments; either list of, map of, or single plain POGO data object is expected
     * @return call status
     */
    Completable call(String rcv, String action, def args)

    Completable call(String rcv, String action, def args, Map<String, ?> headers)

    /**
     * Point-to-point actor action call with arguments.
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     * @param rcv receiver id
     * @param action action id
     * @param args call arguments; either list of, map of, or single plain POGO data object is expected
     * @param opts delivery options
     * @return call status
     */
    Completable call(String rcv, String action, def args, DeliveryOptions opts)

    /**
     * Request-response action call.
     * <p>Single result of this call is emitted when receiver finished processing request and sends response. So this
     * type of call should be used for "call remote action and listen for response".
     * @param rcv receiver id
     * @param action action id
     * @return async call result
     */
    def <T> Maybe<T> request(String rcv, String action)

    /**
     * Request-response actor action call.
     * <p>Single result of this call is emitted when receiver finished processing request and sends response. So this
     * type of call should be used for "call remote action and listen for response".
     * @param rcv receiver id
     * @param action action id
     * @param opts call delivery options
     * @return async call result
     */
    def <T> Maybe<T> request(String rcv, String action, DeliveryOptions opts)

    /**
     * Request-response actor action call with arguments.
     * <p>Single result of this call is emitted when receiver finished processing request and sends response. So this
     * type of call should be used for "call remote action and listen for response".
     * @param rcv receiver id
     * @param action action id
     * @param args call arguments; either list of, map of, or single plain POGO data object is expected
     * @return async call result
     */
    def <T> Maybe<T> request(String rcv, String action, def args)

    /**
     * Request-response actor action call with arguments.
     * <p>Single result of this call is emitted when receiver finished processing request and sends response. So this
     * type of call should be used for "call remote action and listen for response".
     * @param rcv receiver id
     * @param action action id
     * @param args call arguments; either list of, map of, or single plain POGO data object is expected
     * @param opts delivery options
     * @return async call result
     */
    def <T> Maybe<T> request(String rcv, String action, def args, DeliveryOptions opts)

    def <T> Maybe<T> request(String rcv, String action, def args, Map<String, ?> headers)

    /**
     * Publish an event to all registered subscribers.
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     * @param rcv receiver id
     * @param action action id
     * @return call status
     */
    Completable publish(String rcv, String action)

    /**
     * Publish an event to all registered subscribers.
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     * @param rcv receiver id
     * @param action action id
     * @param opts delivery options
     * @return call status
     */
    Completable publish(String rcv, String action, DeliveryOptions opts)

    /**
     * Publish an event to all registered subscribers.
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     * @param rcv receiver id
     * @param action action id
     * @param args call arguments; either list of, map of, or single plain POGO data object is expected
     * @return call status
     */
    Completable publish(String rcv, String action, def args)

    /**
     * Publish an event to all registered subscribers.
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     * @param rcv receiver id
     * @param action action id
     * @param args call arguments; either list of, map of, or single plain POGO data object is expected
     * @param opts delivery options
     * @return call status
     */
    Completable publish(String rcv, String action, def args, DeliveryOptions opts)

    Completable publish(String rcv, String action, def args, Map<String, ?> headers)
}
