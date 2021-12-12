package com.dburyak.vertx.core.eventbus;

import com.dburyak.vertx.core.deployment.spec.Deployment;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Value;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageProducer;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

@Singleton
@Secondary
@Setter(onMethod_ = {@Inject})
public class CallDispatcherDefaultImpl implements CallDispatcher {
    private EventBus eventBus;
    private ArgsCodec argsCodec;
    private Deployment deployment;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@Value("${tools.call.timeout.default:10s}")})
    private Duration defaultCallTimeout;


    @Override
    public Completable send(String action, Object msg, Object args, DeliveryOptions opts) {
        return Completable.fromAction(() -> {
            // get addr by "action"
            // encode args if present
            // set timeout
            // send....
            eventBus.send("addr", "msg", withTimeout(opts));
        });
    }

    @Override
    public <T> MessageProducer<T> sender(String action, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <R> Single<Message<R>> request(String action, Object msg, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <T> Completable reply(Message<T> req, Object msg, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public Completable publish(String action, Object msg, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <T> MessageProducer<T> publisher(String action, Object args, DeliveryOptions opts) {
        return null;
    }

    @Override
    public <T> Single<Disposable> consumer(String action, Handler<Message<T>> doOnCall) {
        return null;
    }

    @Override
    public <T> Single<Disposable> localConsumer(String action, Handler<Message<T>> doOnLocalCall) {
        return null;
    }

    private DeliveryOptions withTimeout(DeliveryOptions opts) {
        if (opts.getSendTimeout() == TIMEOUT_NOT_CONFIGURED) {
            opts.setSendTimeout(defaultCallTimeout.toMillis());
        }
        return opts;
    }

    private DeliveryOptions withArgs(Object args, DeliveryOptions opts) {
        if (args != null) {
            argsCodec.encodeArgs(args, opts.getHeaders());
        }
        return opts;
    }
}
