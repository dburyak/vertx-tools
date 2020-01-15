package com.archiuse.mindis.call

import com.archiuse.mindis.json.JsonHelper
import com.archiuse.mindis.util.MapHelper
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.eventbus.EventBus
import io.vertx.reactivex.servicediscovery.ServiceDiscovery

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Singleton

import static com.archiuse.mindis.call.ServiceType.CALL
import static com.archiuse.mindis.call.ServiceType.PUB_SUB_TOPIC
import static com.archiuse.mindis.call.ServiceType.REQUEST_RESPONSE

@Singleton
@Slf4j
class CallDispatcherEBImpl implements CallDispatcher {
    private static final NO_REPLY_BODY = new Object()

    private Map<String, String> ebAddr = [:]
    private Map<String, ServiceType> serviceTypes = [:]
    private volatile Disposable discoverySubscription

    @Inject
    ServiceDiscovery serviceDiscovery

    @Inject
    ServiceDiscoveryHelper serviceDiscoveryHelper

    @Inject
    EBServiceImplHelper ebServiceImplHelper

    @Inject
    EventBus eventBus

    @Inject
    JsonHelper jsonHelper

    @Inject
    MapHelper mapHelper

    @Property(name = 'mindis.service.discovery.announce-address')
    String discoveryAnnounceAddress

    @Override
    Completable call(String rcv, String action, def args = null, DeliveryOptions opts = null) {
        Completable.fromAction {
            def service = ebServiceImplHelper.buildEbServiceName rcv, action
            def addr = ebAddr[service]
            if (!addr) {
                throw new ServiceNotFoundException(receiver: rcv, action: action)
            }
            if (serviceTypes[service] != CALL) {
                throw new WrongServiceTypeException(receiver: rcv, action: action, expectedType: CALL,
                        actualType: serviceTypes[service])
            }

            if (opts) {
                eventBus.send service, args as Object, opts
            } else {
                eventBus.send service, args
            }
        }
    }

    @Override
    Completable call(String rcv, String action, DeliveryOptions opts) {
        call rcv, action, null, opts
    }

    @Override
    <T> Maybe<T> request(String rcv, String action, def args = null, DeliveryOptions opts = null) {
        Single
                .fromCallable {
                    def service = ebServiceImplHelper.buildEbServiceName rcv, action
                    def addr = ebAddr[service]
                    if (!addr) {
                        throw new ServiceNotFoundException(receiver: rcv, action: action)
                    }
                    if (serviceTypes[service] != REQUEST_RESPONSE) {
                        throw new WrongServiceTypeException(receiver: rcv, action: action, expectedType: REQUEST_RESPONSE,
                                actualType: serviceTypes[service])
                    }
                    addr
                }
                .flatMapMaybe { addr ->
                    def rxRequest = opts ? eventBus.rxRequest(addr, args, opts) : eventBus.rxRequest(addr, args)
                    rxRequest.map { replyMsg ->
                        replyMsg.body() ?: NO_REPLY_BODY
                    }
                    .toMaybe()
                    .filter { it != NO_REPLY_BODY }
                    .map { it as T }
                }
    }

    @Override
    <T> Maybe<T> request(String rcv, String action, DeliveryOptions opts) {
        request rcv, action, null, opts
    }

    @Override
    Completable publish(String rcv, String action, def args = null, DeliveryOptions opts = null) {
        Completable.fromAction {
            def service = ebServiceImplHelper.buildEbServiceName rcv, action
            def addr = ebAddr[service]
            if (!addr) {
                throw new ServiceNotFoundException(receiver: rcv, action: action)
            }
            if (serviceTypes[service] != PUB_SUB_TOPIC) {
                throw new WrongServiceTypeException(receiver: rcv, action: action, expectedType: PUB_SUB_TOPIC,
                        actualType: serviceTypes[service])
            }

            if (opts) {
                eventBus.publish addr, args
            } else {
                eventBus.publish addr, args, opts
            }
        }
    }

    @Override
    Completable publish(String rcv, String action, DeliveryOptions opts) {
        publish rcv, action, null, opts
    }

    @PostConstruct
    protected void init() {
        log.debug 'initialize call dispatcher: {}', this

        // EB services dynamic announcements
        discoverySubscription = Observable
                .create { e ->
                    def ebConsumer = eventBus.consumer(discoveryAnnounceAddress) { msg ->
                        e.onNext msg.body()
                    }
                    e.cancellable = { ebConsumer.unregister() }
                }
                .cast(JsonObject)
                .filter { serviceDiscoveryHelper.isEventBusService it }

        // services in current registry
                .mergeWith(serviceDiscovery
                        .rxGetRecords { serviceDiscoveryHelper.isEventBusService it }
                        .flatMapObservable { recordsList -> Observable.fromIterable(recordsList) }
                        .map { it.toJson() }
                )

        // update local EB addr resolution map
                .map { jsonHelper.fromJson it }
                .subscribe({
                    updateLocalServiceRegistry(it)
                }, {
                    log.error 'error on service discovery', it
                })
    }

    @PreDestroy
    protected void dispose() {
        log.debug 'dispose call dispatcher: {}', this
        discoverySubscription.dispose()
    }

    private void updateLocalServiceRegistry(Map<String, Object> recordMap) {
        switch (recordMap.status) {
            case 'UP':
                def name = recordMap.name
                def addr = recordMap.location[ebServiceImplHelper.keyEbAddr]
                ebAddr[name] = addr
                def type = ServiceType.byTypeName(recordMap.type)
                serviceTypes[name] = type
                log.debug 'register EB service: name={}, type={}, ebAddr={}', name, type, addr
                break
            case ['DOWN', 'OUT_OF_SERVICE']:
                def name = recordMap.name
                def addr = ebAddr.remove name
                def type = serviceTypes.remove name
                log.debug 'unregister EB service: name={}, type={}, ebAddr={}', name, type, addr
                break
            default:
                throw new IllegalArgumentException('unexpected service update status : ' + recordMap.status)
        }
    }
}
