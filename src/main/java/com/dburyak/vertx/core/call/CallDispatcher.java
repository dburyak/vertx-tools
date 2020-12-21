package com.dburyak.vertx.core.call;

import com.archiuse.mindis.call.ServiceType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Dispatcher for all the inter-actor communications.
 * Allows to register call receiver in the system so other actors can call it.
 */
public interface CallDispatcher {

    /**
     * Point-to-point actor action call.
     *
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     *
     * @param request request data
     * @return request status
     */
    Completable notify(@NotNull Request request);

    /**
     * Point-to-point actor action call. Groovy friendly version.
     *
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     *
     * @param requestParams map of request data (keys same as {@link Request} properties names)
     * @return request status
     */
    Completable notify(@NotNull Map<String, Object> requestParams);

    /**
     * Point-to-point actor action call.
     *
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param opts delivery options, includes headers, call timeout and some EB-specific config
     * @return request status
     */
    Completable notify(@NotBlank String action, Object args, Object msg, DeliveryOptions opts);

    /**
     * Point-to-point actor action call with arguments.
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
    Completable notify(@NotBlank String action, Object args, Object msg, Map<String, Object> headers);

    /**
     * Point-to-point request-response actor action call.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param request request data
     * @return response
     */
    Single<Response> request(@NotNull Request request);

    /**
     * Point-to-point request-response actor action call. Groovy friendly version.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param requestParams map of request data (keys same as {@link Request} properties names)
     * @return response
     */
    Single<Response> request(@NotNull Map<String, Object> requestParams);

    /**
     * Point-to-point request-response actor action call.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param opts delivery options, includes headers, call timeout and some EB-specific config
     * @return response
     */
    Single<Response> request(@NotBlank String action, Object args, Object msg, DeliveryOptions opts);

    /**
     * Point-to-point request-response actor action call.
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
    Single<Response> request(@NotBlank String action, Object args, Object msg, Map<String, Object> headers);

    /**
     * Publish an event to all registered subscribers.
     *
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param request request
     * @return request status
     */
    Completable publish(@NotNull Request request);

    /**
     * Publish an event to all registered subscribers. Groovy friendly version.
     *
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param requestParams map of request data (keys same as {@link Request} properties names)
     * @return request status
     */
    Completable publish(@NotNull Map<String, Object> requestParams);

    /**
     * Publish an event to all registered subscribers.
     *
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param action action id
     * @param args call arguments; either list or map is expected; if list is provided, then position of the argument
     * identifies it; if map is provided, then key name of the entry works as argument identifier
     * @param msg message data
     * @param opts delivery options, includes headers, call timeout and some EB-specific config
     * @return response
     */
    Completable publish(@NotBlank String action, Object args, Object msg, DeliveryOptions opts);

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
    Completable publish(@NotBlank String action, Object args, Object msg, Map<String, Object> headers);

    /**
     * Register verticle call handler for {@link CallType#NOTIFICATION} calls.
     *
     * @param action action id
     * @param doOnNotification notification handler
     * @return disposable that allows to unregister call handler
     */
    Single<Disposable> onNotification(@NotBlank String action, @NotNull Handler<Request> doOnNotification);

    /**
     * Register verticle call handler for {@link ServiceType#REQUEST_RESPONSE} calls.
     *
     * @param action action id
     * @param doOnRequest request handler that should return response
     * @return disposable that allows to unregister call handler
     */
    Single<Disposable> onRequest(@NotBlank String action, @NotNull Function<Request, Single<Response>> doOnRequest);

    /**
     * Register verticle subscriber to {@link ServiceType#PUB_SUB_TOPIC} topic with provided event handler.
     *
     * @param action action id
     * @param doOnEvent event handler
     * @return disposable that allows to unsubscribe this subscription to topic
     */
    Single<Disposable> subscribe(@NotBlank String action, @NotNull Handler<Request> doOnEvent);

    /**
     * Set routing configuration.
     *
     * @param routing routing config
     */
    @Inject
    void setRouting(@NotNull RoutingConfig routing);
}
