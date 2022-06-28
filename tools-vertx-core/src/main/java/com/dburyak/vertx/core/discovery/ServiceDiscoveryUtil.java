package com.dburyak.vertx.core.discovery;

import io.micronaut.context.annotation.Property;
import io.reactivex.Observable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Predicate;

@Singleton
public class ServiceDiscoveryUtil {

    @Setter(onParam_ = {@Property(name = "service.discovery.announce-addr")})
    private String discoveryAnnounceAddr;

    @Setter(onParam_ = {@Property(name = "service.discovery.usage-addr")})
    private String discoveryUsageAddr;

    @Setter(onMethod_ = {@Inject})
    private ServiceDiscovery discovery;

    @Setter(onMethod_ = {@Inject})
    private EventBus eventBus;

    public Observable<Record> discover(String serviceName, Predicate<Record> filter) {
        return discovery
                .rxGetRecords(filter::test)
                .flatMapObservable(Observable::fromIterable)
                .concatWith(Observable
                        .<JsonObject>create(emitter -> {
                            var ebConsumer = eventBus.<JsonObject>consumer(discoveryAnnounceAddr,
                                    msg -> emitter.onNext(msg.body()));
                            emitter.setCancellable(ebConsumer::unregister);
                        })
                        .map(Record::new)
                        .filter(filter::test));
    }
}
