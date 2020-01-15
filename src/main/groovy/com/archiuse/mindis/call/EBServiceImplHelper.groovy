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
        [rcv, action].join ebServiceNameActionSeparator
    }

    String buildEbServiceAddr(String rcv, String action, ServiceType type) {
        [type.typeName, rcv, action].join ebAddrSeparator
    }

    /**
     * Split full EB service name into "receiver" and "action" parts.
     * @param ebServiceName full EB service name
     * @return [rcv, action]
     */
    List parseEbServiceName(String ebServiceName) {
        def i = ebServiceName.lastIndexOf(ebServiceNameActionSeparator)
        [ebServiceName[0..<i], ebServiceName[i + 1..-1]]
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
