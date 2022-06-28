package com.dburyak.vertx.core.eventbus;

import io.reactivex.disposables.Disposable;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder(toBuilder = true)
public class EventBusMessageConsumerRegistration<T> implements Disposable {
    private final MessageConsumer<T> ebMsgConsumer;
    private final Record discoveryRecord;
    private final ServiceDiscovery serviceDiscovery;

    @Getter(onMethod_ = {@Override})
    @Builder.Default
    private boolean disposed = false;

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        serviceDiscovery.rxUnpublish(discoveryRecord.getRegistration())
                .andThen(ebMsgConsumer.rxUnregister())
                .subscribe();
    }
}
