package com.archiuse.mindis.call

import com.archiuse.mindis.util.MapHelper
import io.micronaut.context.annotation.Property
import io.reactivex.disposables.Disposable
import io.vertx.reactivex.core.eventbus.Message
import io.vertx.reactivex.core.eventbus.MessageConsumer
import io.vertx.reactivex.servicediscovery.ServiceDiscovery

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EBServiceImplHelper {
    private static final String KEY_LOCATION_EB_SERVICE_ADDR = 'eb_addr'

    @Inject
    ServiceDiscovery serviceDiscovery

    @Inject
    MapHelper mapHelper

    @Property(name = 'mindis.service.eb.name-action-separator')
    String ebServiceNameActionSeparator

    @Property(name = 'mindis.service.eb.addr-separator')
    String ebAddrSeparator

    String getKeyEbAddr() {
        KEY_LOCATION_EB_SERVICE_ADDR
    }

    String buildEbServiceName(String rcv, String action) {
        if (rcv == null) {
            throw new NullPointerException('rcv')
        }
        if (action == null) {
            throw new NullPointerException('action')
        }
        if (ebServiceNameActionSeparator == null) {
            throw new NullPointerException('ebServiceNameActionSeparator')
        }
        if (action.contains(ebServiceNameActionSeparator)) {
            throw new CallSetupException(receiver: rcv, action: action)
        }
        [rcv, action].join ebServiceNameActionSeparator
    }

    String buildEbServiceAddr(String rcv, String action, ServiceType type) {
        if (rcv == null) {
            throw new NullPointerException('rcv')
        }
        if (action == null) {
            throw new NullPointerException('action')
        }
        if (ebAddrSeparator == null) {
            throw new NullPointerException('ebAddrSeparator')
        }
        if (type == null) {
            throw new NullPointerException('type')
        }
        if (action.contains(ebAddrSeparator)) {
            throw new CallSetupException(receiver: rcv, action: action, serviceType: type)
        }
        [type.typeName, rcv, action].join ebAddrSeparator
    }

    /**
     * Split full EB service addr into "type", "receiver" and "action" parts.
     * @param ebServiceAddr full EB service addr
     * @return [rcv, action, type]
     */
    List parseEbServiceAddr(String ebServiceAddr) {
        def idxLast = ebServiceAddr.lastIndexOf(ebAddrSeparator)
        def idxFirst = ebServiceAddr.indexOf(ebAddrSeparator)
        if (idxFirst < 0 || idxLast < 0 || !(idxFirst < idxLast)) {
            throw new MalformedEbAddressNameException(addr: ebServiceAddr)
        }
        def sepLen = ebAddrSeparator.size()
        try {
            def type = ServiceType.byTypeName(ebServiceAddr[0..<idxFirst])
            if (!type) {
                throw new MalformedEbAddressNameException(addr: ebServiceAddr)
            }
            [ebServiceAddr[idxFirst + sepLen..<idxLast],
             ebServiceAddr[idxLast + sepLen..-1],
             type]
        } catch (ignored) {
            throw new MalformedEbAddressNameException(addr: ebServiceAddr)
        }
    }

    Disposable toDisposable(MessageConsumer ebConsumer, String serviceRegistrationId) {
        new EBServiceSubscription(ebConsumer: ebConsumer, serviceRegistrationId: serviceRegistrationId,
                serviceDiscovery: serviceDiscovery)
    }

    def toClosureArgs(Message msg) {
        [msg.body(), mapHelper.toMap(msg.headers())]
    }

    private static final class EBServiceSubscription implements Disposable {
        boolean disposed = false
        MessageConsumer ebConsumer
        String serviceRegistrationId
        ServiceDiscovery serviceDiscovery

        @Override
        void dispose() {
            if (disposed) {
                return
            }
            disposed = true
            serviceDiscovery
                    .rxUnpublish(serviceRegistrationId)
                    .andThen(ebConsumer.rxUnregister())
                    .subscribe()
        }
    }
}
