package com.dburyak.vertx.core.call;

import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Value;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageProducer;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Map;

import static com.dburyak.vertx.core.call.CallType.NOTIFICATION;
import static com.dburyak.vertx.core.call.CallType.PUB_SUB_TOPIC;
import static com.dburyak.vertx.core.call.CallType.REQUEST_RESPONSE;


/**
 * Default implementation.
 * Is just a thin wrapper over {@link EventBus} with integration with {@link Routing}, address validation, and syntactic
 * sugar.
 */
@Singleton
@Secondary
@Slf4j
public class CallDispatcherImpl implements CallDispatcher {

    /**
     * Key - action id, value - eb addr discovered over service discovery.
     */
    private Map<String, String> ebAddr;

    private Routing routing;

    @Setter(onMethod_ = {@Inject})
    private EventBus eventBus;

    @Setter(onMethod_ = {@Inject})
    private MessageCodec<Object, Object> msgCodec;

    @Setter(onMethod_ = {@Inject})
    private ArgsCodec argsCodec;

    @Setter(onMethod_ = {@Inject})
    private ServiceDiscovery serviceDiscovery;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@Value("${call.dispatcher.notification.timeout.default:30s}")})
    private Duration defaultNotificationTimeout;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@Value("${call.dispatcher.request.timeout.default:30s}")})
    private Duration defaultRequestTimeout;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@Value("${call.dispatcher.publish.timeout.default:30s}")})
    private Duration defaultPublishTimeout;


    @Override
    public Completable notify(@NotBlank String action, Object args, Object msg, DeliveryOptions opts) {
        return Completable.fromAction(() -> {
            verifyCallTypeIs(NOTIFICATION, action);
            var addr = getAddrForAction(action);
            var deliveryOpts = buildDeliveryOptions(NOTIFICATION, opts, args);
            eventBus.send(addr, msg, deliveryOpts);
        });
    }

    @Override
    public <T> MessageProducer<T> notifier(@NotBlank String action, Object args, DeliveryOptions opts) {
        verifyCallTypeIs(NOTIFICATION, action);
        var addr = getAddrForAction(action);
        var deliveryOpts = buildDeliveryOptions(NOTIFICATION, opts, args);
        return eventBus.sender(addr, deliveryOpts);
    }

    @Override
    public Maybe<Response> request(@NotBlank String action, Object args, Object msg, DeliveryOptions opts) {
        return Single
                .fromCallable(() -> {
                    verifyCallTypeIs(REQUEST_RESPONSE, action);
                    return getAddrForAction(action);
                })
                .flatMapMaybe(addr -> eventBus
                        .rxRequest(addr, msg, buildDeliveryOptions(REQUEST_RESPONSE, opts, args))
                        .toMaybe()
                        .filter(m -> m.body() != null)
                        .map(respVertxMsg -> Response.builder()
                                .msg(respVertxMsg.body())
                                .headers(respVertxMsg.headers())
                                .build()));
    }

    @Override
    public Completable publish(@NotBlank String action, Object args, Object msg, DeliveryOptions opts) {
        return Completable.fromAction(() -> {
            verifyCallTypeIs(PUB_SUB_TOPIC, action);
            var addr = getAddrForAction(action);
            var deliveryOpts = buildDeliveryOptions(PUB_SUB_TOPIC, opts, args);
            eventBus.publish(addr, msg, deliveryOpts);
        });
    }

    @Override
    public <T> MessageProducer<T> publisher(@NotBlank String action, Object args, DeliveryOptions opts) {
        verifyCallTypeIs(PUB_SUB_TOPIC, action);
        var addr = getAddrForAction(action);
        var deliveryOpts = buildDeliveryOptions(PUB_SUB_TOPIC, opts, args);
        return eventBus.publisher(addr, deliveryOpts);
    }

    @Override
    public <T> Single<Disposable> onNotification(@NotBlank String action,
            @NotNull Handler<Request<T>> doOnNotification) {
        return Single
                // register EB consumer
                .fromCallable(() -> {
                    verifyCallTypeIs(NOTIFICATION, action);
                    var addr = getAddrForAction(action);
                    return eventBus.<T>consumer(addr, msg -> {
                        var req = toRequest(action, msg);
                        doOnNotification.handle(req);
                    });
                })

                // publish service discovery record
                .flatMap(ebMsgConsumer -> serviceDiscovery.rxPublish(buildServiceDiscoveryRecord(NOTIFICATION, action))
                        .doOnSuccess(reg -> log.debug("eb action registered: action={}, type={}, addr={}, regId={}",
                                action, NOTIFICATION, reg.getLocation(), reg.getRegistration()))

                        // build disposable to be able to unregister this service and unsubscribe EB consumer
                        .map(reg -> EbActionRegistration.<T>builder()
                                .ebMsgConsumer(ebMsgConsumer)
                                .discoveryRecord(reg)
                                .serviceDiscovery(serviceDiscovery)
                                .build()));
    }

    @Override
    public <T> Single<Disposable> onRequest(@NotBlank String action,
            @NotNull Function<Request<T>, Single<Response>> doOnRequest) {

        // TODO: implement
        return null;
    }

    @Override
    public <T> Single<Disposable> subscribe(@NotBlank String action, @NotNull Handler<Request<T>> doOnEvent) {
        // TODO: implement
        return null;
    }

    @Inject
    public void setRouting(Routing routing) {
        this.routing = routing;
        // TODO: build ebAddr map here
    }

    private String getAddrForAction(String action) {
        var addr = ebAddr.get(action);
        if (addr == null) {
            throw new IllegalArgumentException("action not registered: " + action);
        }
        return addr;
    }

    private void verifyCallTypeIs(CallType expectedCallType, String action) {
        var route = routing.getRoutes().get(action);
        if (route == null) {
            log.warn("trying to verify call type of unknown action: action={}, expectedCallType={}",
                    action, expectedCallType);
            throw new IllegalArgumentException("route for action is not registered: action={}" + action);
        }
        var actionCallType = route.getCallType();
        if (!expectedCallType.equals(actionCallType)) {
            log.warn("action has different call type than called: action={}, actionCallType={}, calledAs={}",
                    action, actionCallType, expectedCallType);
            throw new IllegalArgumentException("action has different call type than called: action=" + action +
                    ", actionCallType=" + actionCallType + ", " + "calledAs=" + expectedCallType);
        }
    }

    private DeliveryOptions buildDeliveryOptions(CallType callType, DeliveryOptions providedOpts, Object args) {
        DeliveryOptions deliveryOpts;
        if (providedOpts != null) {
            deliveryOpts = providedOpts;
            if (deliveryOpts.getSendTimeout() == TIMEOUT_NOT_CONFIGURED) {
                var callTimeout = (callType == NOTIFICATION) ? defaultNotificationTimeout.toMillis()
                        : (callType == REQUEST_RESPONSE) ? defaultRequestTimeout.toMillis()
                        : defaultPublishTimeout.toMillis();
                deliveryOpts.setSendTimeout(callTimeout);
            }
        } else {
            deliveryOpts = new DeliveryOptions().setSendTimeout(defaultRequestTimeout.toMillis());
        }
        deliveryOpts.setCodecName(msgCodec.name());
        argsCodec.encodeArgs(args, deliveryOpts.getHeaders());
        return deliveryOpts;
    }

    private <T> Request<T> toRequest(String action, Message<T> ebMsg) {
        return Request.<T>builder()
                .action(action)
                .msg(ebMsg.body())
                .args(argsCodec.decodeArgs(ebMsg.headers().getDelegate()))
                .deliveryOptions(null)
                .headers(ebMsg.headers().getDelegate())
                .build();
    }

    private Record buildServiceDiscoveryRecord(CallType callType, String action) {
        return new Record().setName(action)
                .setType(callType.name())
                .setLocation(new JsonObject()
                        .put(DISCOVERY_LOCATION_KEY_EB_ADDR, getAddrForAction(action)));
    }
}
