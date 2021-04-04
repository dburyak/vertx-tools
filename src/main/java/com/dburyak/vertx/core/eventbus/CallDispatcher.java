package com.dburyak.vertx.core.eventbus;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageProducer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Wrapper around {@link io.vertx.core.eventbus.EventBus} that integrates with the toolkit. Should be used for all the
 * EventBus communications unless something very customized is needed.
 * <p>
 * Integrates with deployment configuration and all the codecs associated.
 */
public interface CallDispatcher {

    /**
     * Magic number to mark message delivery timeout in {@link DeliveryOptions} as "not specified" instead of using
     * default 30 seconds.
     */
    long TIMEOUT_NOT_CONFIGURED = Long.MAX_VALUE - 2L;


    /**
     * Point-to-point actor notification call.
     *
     * <p>Action is called asynchronously and is completed as soon as it is sent out successfully.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases, and more suitable for
     * local-only communications.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param opts delivery options, includes headers, call timeout and some EB-specific config
     * @return request status
     */
    Completable send(@NotBlank String action, Object msg, Object args, DeliveryOptions opts);

    default Completable send(@NotBlank String action, Object msg, DeliveryOptions opts) {
        return send(action, msg, null, opts);
    }

    default Completable send(@NotBlank String action, DeliveryOptions opts) {
        return send(action, null, null, opts);
    }

    default Completable send(@NotBlank String action, Object msg, Object args) {
        return send(action, msg, args, emptyOptions());
    }

    default Completable send(@NotBlank String action, Object msg) {
        return send(action, msg, null, emptyOptions());
    }

    default Completable send(@NotBlank String action) {
        return send(action, null, null, emptyOptions());
    }

    <T> MessageProducer<T> sender(@NotBlank String action, Object args, DeliveryOptions opts);

    default <T> MessageProducer<T> sender(@NotBlank String action, DeliveryOptions opts) {
        return sender(action, null, opts);
    }

    default <T> MessageProducer<T> sender(@NotBlank String action, Object args) {
        return sender(action, args, emptyOptions());
    }

    default <T> MessageProducer<T> sender(@NotBlank String action) {
        return sender(action, null, emptyOptions());
    }

    /**
     * Point-to-point actor notification call with arguments.
     *
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param headers headers
     * @return request status
     */
    default Completable send(@NotBlank String action, Object msg, Object args, MultiMap headers) {
        return send(action, args, msg, emptyOptionsWithHeaders(headers));
    }

    default Completable send(@NotBlank String action, Object msg, MultiMap headers) {
        return send(action, msg, null, emptyOptionsWithHeaders(headers));
    }

    default Completable send(@NotBlank String action, MultiMap headers) {
        return send(action, null, null, emptyOptionsWithHeaders(headers));
    }

    default <T> MessageProducer<T> sender(@NotBlank String action, Object args, MultiMap headers) {
        return sender(action, args, emptyOptionsWithHeaders(headers));
    }

    default <T> MessageProducer<T> sender(@NotBlank String action, MultiMap headers) {
        return sender(action, null, emptyOptionsWithHeaders(headers));
    }

    /**
     * Point-to-point actor notification call with arguments.
     *
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param headers headers
     * @return request status
     */
    default Completable send(@NotBlank String action, Object msg, Object args, Map<String, String> headers) {
        return send(action, msg, args, emptyOptionsWithHeaders(headers));
    }

    default Completable send(@NotBlank String action, Object msg, Map<String, String> headers) {
        return send(action, msg, null, emptyOptionsWithHeaders(headers));
    }

    default Completable send(@NotBlank String action, Map<String, String> headers) {
        return send(action, null, null, emptyOptionsWithHeaders(headers));
    }

    default <T> MessageProducer<T> sender(@NotBlank String action, Object args, Map<String, String> headers) {
        return sender(action, args, emptyOptionsWithHeaders(headers));
    }

    default <T> MessageProducer<T> sender(@NotBlank String action, Map<String, String> headers) {
        return sender(action, null, emptyOptionsWithHeaders(headers));
    }


    /**
     * Point-to-point request-response actor call.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param opts delivery options, includes headers, call timeout and some EB-specific config
     * @return response message
     */
    <R> Single<Message<R>> request(@NotBlank String action, Object msg, Object args, DeliveryOptions opts);

    default <R> Single<Message<R>> request(@NotBlank String action, Object msg, DeliveryOptions opts) {
        return request(action, msg, null, opts);
    }

    default <R> Single<Message<R>> request(@NotBlank String action, DeliveryOptions opts) {
        return request(action, null, null, opts);
    }

    default <R> Single<Message<R>> request(@NotBlank String action, Object msg, Object args) {
        return request(action, msg, args, emptyOptions());
    }

    default <R> Single<Message<R>> request(@NotBlank String action, Object msg) {
        return request(action, msg, null, emptyOptions());
    }

    default <R> Single<Message<R>> request(@NotBlank String action) {
        return request(action, null, null, emptyOptions());
    }

    /**
     * Point-to-point request-response actor call.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param headers headers
     * @return response
     */
    default <R> Single<Message<R>> request(@NotBlank String action, Object msg, Object args, MultiMap headers) {
        return request(action, args, msg, emptyOptionsWithHeaders(headers));
    }

    default <R> Single<Message<R>> request(@NotBlank String action, Object msg, MultiMap headers) {
        return request(action, msg, null, emptyOptionsWithHeaders(headers));
    }

    default <R> Single<Message<R>> request(@NotBlank String action, MultiMap headers) {
        return request(action, null, null, emptyOptionsWithHeaders(headers));
    }

    // TODO: add "reply(Message<T>)" method here to be used in request handlers

    /**
     * Point-to-point request-response actor call.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param headers headers
     * @return response
     */
    default <R> Single<Message<R>> request(@NotBlank String action, Object msg, Object args,
            Map<String, String> headers) {
        return request(action, msg, args, emptyOptionsWithHeaders(headers));
    }

    default <R> Single<Message<R>> request(@NotBlank String action, Object msg, Map<String, String> headers) {
        return request(action, msg, null, emptyOptionsWithHeaders(headers));
    }

    default <R> Single<Message<R>> request(@NotBlank String action, Map<String, String> headers) {
        return request(action, null, null, emptyOptionsWithHeaders(headers));
    }


    <T> Completable reply(@NotNull Message<T> req, Object msg, Object args, DeliveryOptions opts);

    default <T> Completable reply(@NotNull Message<T> req, Object msg, DeliveryOptions opts) {
        return reply(req, msg, null, opts);
    }

    default <T> Completable reply(@NotNull Message<T> req, DeliveryOptions opts) {
        return reply(req, null, null, opts);
    }

    default <T> Completable reply(@NotNull Message<T> req, Object msg, Object args) {
        return reply(req, msg, args, emptyOptions());
    }

    default <T> Completable reply(@NotNull Message<T> req, Object msg) {
        return reply(req, msg, null, emptyOptions());
    }

    default <T> Completable reply(@NotNull Message<T> req) {
        return reply(req, null, null, emptyOptions());
    }

    default <T> Completable reply(@NotNull Message<T> req, Object msg, Object args, MultiMap headers) {
        return reply(req, msg, args, emptyOptionsWithHeaders(headers));
    }

    default <T> Completable reply(@NotNull Message<T> req, Object msg, MultiMap headers) {
        return reply(req, msg, null, emptyOptionsWithHeaders(headers));
    }

    default <T> Completable reply(@NotNull Message<T> req, MultiMap headers) {
        return reply(req, null, null, emptyOptionsWithHeaders(headers));
    }

    default <T> Completable reply(@NotNull Message<T> req, Object msg, Object args, Map<String, String> headers) {
        return reply(req, msg, args, emptyOptionsWithHeaders(headers));
    }

    default <T> Completable reply(@NotNull Message<T> req, Object msg, Map<String, String> headers) {
        return reply(req, msg, null, emptyOptionsWithHeaders(headers));
    }

    default <T> Completable reply(@NotNull Message<T> req, Map<String, String> headers) {
        return reply(req, null, null, emptyOptionsWithHeaders(headers));
    }


    /**
     * Publish an event to all registered subscribers.
     *
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the
     * argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param opts delivery options, includes headers, call timeout and some EB-specific config
     * @return response
     */
    Completable publish(@NotBlank String action, Object msg, Object args, DeliveryOptions opts);

    default Completable publish(@NotBlank String action, Object msg, DeliveryOptions opts) {
        return publish(action, msg, null, opts);
    }

    default Completable publish(@NotBlank String action, DeliveryOptions opts) {
        return publish(action, null, null, opts);
    }

    default Completable publish(@NotBlank String action, Object msg, Object args) {
        return publish(action, msg, args, emptyOptions());
    }

    default Completable publish(@NotBlank String action, Object msg) {
        return publish(action, msg, null, emptyOptions());
    }

    default Completable publish(@NotBlank String action) {
        return publish(action, null, null, emptyOptions());
    }

    <T> MessageProducer<T> publisher(@NotBlank String action, Object args, DeliveryOptions opts);

    default <T> MessageProducer<T> publisher(@NotBlank String action, DeliveryOptions opts) {
        return publisher(action, null, opts);
    }

    default <T> MessageProducer<T> publisher(@NotBlank String action, Object args) {
        return publisher(action, args, emptyOptions());
    }

    default <T> MessageProducer<T> publisher(@NotBlank String action) {
        return publisher(action, null, emptyOptions());
    }

    /**
     * Publish an event to all registered subscribers.
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param headers headers
     * @return response
     */
    default Completable publish(@NotBlank String action, Object msg, Object args, MultiMap headers) {
        return publish(action, msg, args, emptyOptionsWithHeaders(headers));
    }

    default Completable publish(@NotBlank String action, Object msg, MultiMap headers) {
        return publish(action, msg, null, emptyOptionsWithHeaders(headers));
    }

    default Completable publish(@NotBlank String action, MultiMap headers) {
        return publish(action, null, null, emptyOptionsWithHeaders(headers));
    }

    /**
     * Publish an event to all registered subscribers.
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param headers headers
     * @return response
     */
    default Completable publish(@NotBlank String action, Object msg, Object args, Map<String, String> headers) {
        return publish(action, msg, args, emptyOptionsWithHeaders(headers));
    }

    default Completable publish(@NotBlank String action, Object msg, Map<String, String> headers) {
        return publish(action, msg, null, emptyOptionsWithHeaders(headers));
    }

    default Completable publish(@NotBlank String action, Map<String, String> headers) {
        return publish(action, null, null, emptyOptionsWithHeaders(headers));
    }

    /**
     * Register verticle action handler for incoming calls.
     *
     * @param action action id
     * @param doOnCall message handler
     * @return disposable that allows to unregister call handler
     */
    <T> Single<Disposable> consumer(@NotBlank String action, @NotNull Handler<Message<T>> doOnCall);

    <T> Single<Disposable> localConsumer(@NotBlank String action, @NotNull Handler<Message<T>> doOnLocalCall);

    private DeliveryOptions emptyOptions() {
        return new DeliveryOptions().setSendTimeout(TIMEOUT_NOT_CONFIGURED);
    }

    private DeliveryOptions emptyOptionsWithHeaders(MultiMap headers) {
        return emptyOptions().setHeaders(headers);
    }

    private DeliveryOptions emptyOptionsWithHeaders(Map<String, String> headers) {
        return emptyOptions().setHeaders(MultiMap.caseInsensitiveMultiMap().addAll(headers));
    }
}
