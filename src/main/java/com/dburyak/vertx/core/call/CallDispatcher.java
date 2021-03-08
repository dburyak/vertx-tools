package com.dburyak.vertx.core.call;

import com.archiuse.mindis.call.ServiceType;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.eventbus.MessageProducer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Dispatcher for all the inter-actor communications.
 * Allows to register call receiver in the system so other actors can call it.
 */
public interface CallDispatcher {
    String MAP_KEY_ACTION = "action";
    String MAP_KEY_ARGS = "args";
    String MAP_KEY_MSG = "msg";
    String MAP_KEY_DELIVERY_OPTIONS = "deliveryOptions";
    String MAP_KEY_HEADERS = "headers";
    long TIMEOUT_NOT_CONFIGURED = Long.MAX_VALUE - 2L;
    String DISCOVERY_LOCATION_KEY_EB_ADDR = "addr";

    /**
     * Point-to-point actor notification call.
     *
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     *
     * @param request request data
     * @return request status
     */
    default <T> Completable notify(@NotNull Request<T> request) {
        var hasDeliveryOptions = request.getDeliveryOptions() != null;
        var hasHeaders = request.getHeaders() != null;
        if (hasDeliveryOptions || !hasHeaders) {
            return notify(request.getAction(), request.getArgs(), request.getMsg(), request.getDeliveryOptions());
        } else {
            return notify(request.getAction(), request.getArgs(), request.getMsg(), request.getHeaders());
        }
    }

    /**
     * Point-to-point actor notification call. Groovy friendly version.
     *
     * <p>Action is called asynchronously and is completed as soon as it is delivered successfully to the receiver.
     * Errors that happen on receiver side during actual call processing are not reported to the caller. So
     * this type of call should be used only for "trigger remote action and forget" cases.
     *
     * @param requestParams map of request data (keys same as {@link Request} properties names)
     * @return request status
     */
    default Completable notify(@NotNull Map<String, Object> requestParams) {
        return Single.just(requestParams)
                .map(this::toRequest)
                .flatMapCompletable(this::notify);
    }

    /**
     * Point-to-point actor notification call.
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

    <T> MessageProducer<T> notifier(@NotBlank String action, Object args, DeliveryOptions opts);

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
    default Completable notify(@NotBlank String action, Object args, Object msg, Map<String, String> headers) {
        return Single.just(headers)
                .map(h -> MultiMap.caseInsensitiveMultiMap().addAll(h))
                .flatMapCompletable(h -> notify(action, args, msg, h));
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
    default Completable notify(@NotBlank String action, Object args, Object msg, MultiMap headers) {
        return notify(action, args, msg, new DeliveryOptions().setHeaders(headers)
                .setSendTimeout(TIMEOUT_NOT_CONFIGURED));
    }

    /**
     * Point-to-point request-response actor action call.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param request request data
     * @return response
     */
    default <T> Maybe<Response> request(@NotNull Request<T> request) {
        var hasDeliveryOptions = request.getDeliveryOptions() != null;
        var hasHeaders = request.getHeaders() != null;
        if (hasDeliveryOptions || !hasHeaders) {
            return request(request.getAction(), request.getArgs(), request.getMsg(), request.getDeliveryOptions());
        } else {
            return request(request.getAction(), request.getArgs(), request.getMsg(), request.getHeaders());
        }
    }

    /**
     * Point-to-point request-response actor call. Groovy friendly version.
     *
     * <p>Single result of this call is emitted when receiver finished processing request and sent response. So this
     * type of call should be used for "call remote action and wait for response".
     *
     * @param requestParams map of request data (keys same as {@link Request} properties names)
     * @return response
     */
    default Maybe<Response> request(@NotNull Map<String, Object> requestParams) {
        return Single.just(requestParams)
                .map(this::toRequest)
                .flatMapMaybe(this::request);
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
     * @return response
     */
    Maybe<Response> request(@NotBlank String action, Object args, Object msg, DeliveryOptions opts);

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
    default Maybe<Response> request(@NotBlank String action, Object args, Object msg, Map<String, String> headers) {
        return Single.just(headers)
                .map(h -> MultiMap.caseInsensitiveMultiMap().addAll(h))
                .flatMapMaybe(h -> request(action, args, msg, h));
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
    default Maybe<Response> request(@NotBlank String action, Object args, Object msg, MultiMap headers) {
        return request(action, args, msg, new DeliveryOptions().setHeaders(headers)
                .setSendTimeout(TIMEOUT_NOT_CONFIGURED));
    }

    /**
     * Publish an event to all registered subscribers.
     *
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param request request
     * @return request status
     */
    default <T> Completable publish(@NotNull Request<T> request) {
        var hasDeliveryOptions = request.getDeliveryOptions() != null;
        var hasHeaders = request.getHeaders() != null;
        if (hasDeliveryOptions || !hasHeaders) {
            return publish(request.getAction(), request.getArgs(), request.getMsg(), request.getDeliveryOptions());
        } else {
            return publish(request.getAction(), request.getArgs(), request.getMsg(), request.getHeaders());
        }
    }

    /**
     * Publish an event to all registered subscribers. Groovy friendly version.
     *
     * <p>This is one-to-many event communication. This type of call
     * should be used for "notify all registered subscribers about event" case.
     *
     * @param requestParams map of request data (keys same as {@link Request} properties names)
     * @return request status
     */
    default Completable publish(@NotNull Map<String, Object> requestParams) {
        return Single.just(requestParams)
                .map(this::toRequest)
                .flatMapCompletable(this::publish);
    }

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

    <T> MessageProducer<T> publisher(@NotBlank String action, Object args, DeliveryOptions opts);

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
    default Completable publish(@NotBlank String action, Object args, Object msg, Map<String, String> headers) {
        return Single.just(headers)
                .map(h -> MultiMap.caseInsensitiveMultiMap().addAll(h))
                .flatMapCompletable(h -> publish(action, args, msg, h));
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
    default Completable publish(@NotBlank String action, Object args, Object msg, MultiMap headers) {
        return publish(action, args, msg, new DeliveryOptions().setHeaders(headers)
                .setSendTimeout(TIMEOUT_NOT_CONFIGURED));
    }

    /**
     * Register verticle call handler for {@link CallType#NOTIFICATION} calls.
     *
     * @param action action id
     * @param doOnNotification notification handler
     * @return disposable that allows to unregister call handler
     */
    <T> Single<Disposable> onNotification(@NotBlank String action, @NotNull Handler<Request<T>> doOnNotification);

    /**
     * Register verticle call handler for {@link ServiceType#REQUEST_RESPONSE} calls.
     *
     * @param action action id
     * @param doOnRequest request handler that should return response
     * @return disposable that allows to unregister call handler
     */
    <T> Single<Disposable> onRequest(@NotBlank String action, @NotNull Function<Request<T>,
            Single<Response>> doOnRequest);

    /**
     * Register verticle subscriber to {@link ServiceType#PUB_SUB_TOPIC} topic with provided event handler.
     *
     * @param action action id
     * @param doOnEvent event handler
     * @return disposable that allows to unsubscribe this subscription to topic
     */
    <T> Single<Disposable> subscribe(@NotBlank String action, @NotNull Handler<Request<T>> doOnEvent);

    private Request<Object> toRequest(Map<String, Object> requestParams) {
        MultiMap headers = null;
        var headersParam = requestParams.get(MAP_KEY_HEADERS);
        if (headersParam != null) {
            if (headersParam instanceof MultiMap) {
                headers = (MultiMap) headersParam;
            } else if (headersParam instanceof io.vertx.reactivex.core.MultiMap) {
                headers = ((io.vertx.reactivex.core.MultiMap) headersParam).getDelegate();
            } else if (headersParam instanceof Map) {
                headers = MultiMap.caseInsensitiveMultiMap().addAll((Map<String, String>) headersParam);
            } else {
                throw new IllegalArgumentException("headers are of unsupported type: " + headersParam.getClass());
            }
        }
        return Request.builder()
                .action((String) requestParams.get(MAP_KEY_ACTION))
                .args(requestParams.get(MAP_KEY_ARGS))
                .msg(requestParams.get(MAP_KEY_MSG))
                .headers(headers)
                .deliveryOptions((DeliveryOptions) requestParams.get(MAP_KEY_DELIVERY_OPTIONS))
                .build();
    }
}
