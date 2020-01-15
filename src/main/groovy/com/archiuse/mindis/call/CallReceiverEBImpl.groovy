package com.archiuse.mindis.call

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.eventbus.EventBus
import io.vertx.reactivex.core.eventbus.MessageConsumer
import io.vertx.reactivex.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.Record

import javax.inject.Inject
import javax.inject.Singleton

import static com.archiuse.mindis.call.CommunicationType.VERTX_EVENT_BUS_P2P
import static com.archiuse.mindis.call.CommunicationType.VERTX_EVENT_BUS_TOPIC
import static com.archiuse.mindis.call.ServiceType.CALL
import static com.archiuse.mindis.call.ServiceType.PUB_SUB_TOPIC
import static com.archiuse.mindis.call.ServiceType.REQUEST_RESPONSE

@Singleton
@Slf4j
class CallReceiverEBImpl implements CallReceiver {

    @Inject
    ServiceDiscovery serviceDiscovery

    @Inject
    EventBus eventBus

    @Inject
    EBServiceImplHelper ebServiceImplHelper

    @Inject
    ServiceDiscoveryHelper serviceDiscoveryHelper

    @Override
    Single<Disposable> onCall(String rcv, String action, @ClosureParams(value = SimpleType,
            options = ['java.lang.Object', 'java.util.Map<java.lang.String, java.util.List>'])
            Closure<Void> doOnCall) {

        Single

        // register EB consumer
                .fromCallable {
                    def ebAddr = ebServiceImplHelper.buildEbServiceAddr rcv, action, CALL
                    def ebConsumer = eventBus.consumer(ebAddr) { msg ->
                        doOnCall(ebServiceImplHelper.toClosureArgs(msg))
                    }
                    def serviceName = ebServiceImplHelper.buildEbServiceName rcv, action
                    [name: serviceName, addr: ebAddr, consumer: ebConsumer]
                }

        // publish service record
                .flatMap { params ->
                    def record = buildServiceRecord(params.name as String, params.addr as String, CALL,
                            VERTX_EVENT_BUS_P2P)
                    serviceDiscovery.rxPublish(record)
                            .map { params + [record: it] }
                            .doOnSuccess {
                                log.debug 'service registered: rcv={}, action={}, type={}, ebAddr={}',
                                        rcv, action, CALL, params.addr
                            }
                }

        // build disposable to be able to unregister this service and unsubscribe EB consumer
                .map {
                    ebServiceImplHelper.toDisposable(it.consumer as MessageConsumer, it.record.registration as String)
                }
    }

    @Override
    <R> Single<Disposable> onRequest(String rcv, String action, @ClosureParams(value = SimpleType,
            options = ['java.lang.Object', 'java.util.Map<java.lang.String, java.util.List>'])
            Closure<Maybe<R>> doOnRequest) {

        Single

        // register EB consumer
                .fromCallable {
                    def ebAddr = ebServiceImplHelper.buildEbServiceAddr rcv, action, REQUEST_RESPONSE
                    def ebConsumer = eventBus.consumer(ebAddr) { msg ->
                        doOnRequest(ebServiceImplHelper.toClosureArgs(msg))
                                .subscribe({
                                    msg.reply it
                                }, {
                                    msg.fail(0, "${it.class} : ${it.message}")
                                }, {
                                    msg.reply(null)
                                })
                    }
                    def serviceName = ebServiceImplHelper.buildEbServiceName rcv, action
                    [name: serviceName, addr: ebAddr, consumer: ebConsumer]
                }

        // publish service record
                .flatMap { params ->
                    def record = buildServiceRecord(params.name as String, params.addr as String, REQUEST_RESPONSE,
                            VERTX_EVENT_BUS_P2P)
                    serviceDiscovery.rxPublish(record)
                            .map { params + [record: it] }
                            .doOnSuccess {
                                log.debug 'service registered: rcv={}, action={}, type={}, ebAddr={}',
                                        rcv, action, REQUEST_RESPONSE, params.addr
                            }
                }

        // build disposable to be able to unregister this service and unsubscribe EB consumer
                .map {
                    ebServiceImplHelper.toDisposable(it.consumer as MessageConsumer, it.record.registration as String)
                }
    }

    @Override
    Single<Disposable> subscribe(String rcv, String action, @ClosureParams(value = SimpleType,
            options = ['java.lang.Object', 'java.util.Map<java.lang.String, java.util.List>'])
            Closure<Void> doOnEvent) {

        Single

        // register EB consumer
                .fromCallable {
                    def ebAddr = ebServiceImplHelper.buildEbServiceAddr rcv, action, PUB_SUB_TOPIC
                    def ebConsumer = eventBus.consumer(ebAddr) { msg ->
                        doOnEvent(ebServiceImplHelper.toClosureArgs(msg))
                    }
                    def serviceName = ebServiceImplHelper.buildEbServiceName rcv, action
                    [name: serviceName, addr: ebAddr, consumer: ebConsumer]
                }

        // publish service record
                .flatMap { params ->
                    def record = buildServiceRecord(params.name as String, params.addr as String, PUB_SUB_TOPIC,
                            VERTX_EVENT_BUS_TOPIC)
                    serviceDiscovery.rxPublish(record)
                            .map { params + [record: it] }
                            .doOnSuccess {
                                log.debug 'service registered: rcv={}, action={}, type={}, ebAddr={}',
                                        rcv, action, PUB_SUB_TOPIC, params.addr
                            }
                }

        // build disposable to be able to unregister this service and unsubscribe EB consumer
                .map {
                    ebServiceImplHelper.toDisposable(it.consumer as MessageConsumer, it.record.registration as String)
                }
    }

    private Record buildServiceRecord(String name, String ebAddr, ServiceType serviceType, CommunicationType commType) {
        new Record().tap {
            it.name = name
            type = serviceType.typeName
            location = new JsonObject()
                    .put(ebServiceImplHelper.keyEbAddr, ebAddr)
            metadata = new JsonObject()
                    .put(serviceDiscoveryHelper.keyCommType, commType.typeName)
        }
    }
}
