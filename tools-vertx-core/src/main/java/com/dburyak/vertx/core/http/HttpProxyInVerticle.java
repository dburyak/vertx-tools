package com.dburyak.vertx.core.http;

import com.dburyak.vertx.core.DiVerticle;
import com.dburyak.vertx.core.VerticleProducer;
import com.dburyak.vertx.core.deployment.spec.Deployment;
import com.dburyak.vertx.core.deployment.spec.Verticles;
import com.dburyak.vertx.core.eventbus.CallDispatcher;
import io.micronaut.context.annotation.Value;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

/**
 * Proxy verticle for receiving http calls from other verticles.
 * <p>
 * Not designed to be used for public API. Designed to be used only when there's a need to replace verticle-to-verticle
 * communication via EventBus with communication over http. Should be used only in cases when rearranging verticles in
 * deployments and it is no longer possible for two verticles to communicate directly via EventBus.
 * <p>
 * For developing public APIs (or stable private APIs) use default vertx facilities for rest api development.
 */
@Singleton
@Setter(onMethod_ = {@Inject})
public class HttpProxyInVerticle extends DiVerticle {
    private Deployment deployment;
    private HttpServer httpServer;
    private Router httpRouter;
    private CallDispatcher callDispatcher;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@Value("${tools.proxy.http.in.port:80}")})
    private int port;

    @Setter(onMethod_ = {@Inject}, onParam_ = {@Value("${tools.proxy.http.in.host:}")})
    private String host;

    @Setter
    private com.dburyak.vertx.core.deployment.spec.HttpProxyInVerticle verticleSpec;

    @Override
    protected Completable doOnStart() {
        return Observable.fromIterable(verticleSpec.getOutActions().getList())
                .doOnNext(this::addActionToRouter)
                .ignoreElements()
                .andThen(Completable.defer(() -> {
                            httpServer.requestHandler(httpRouter);
                            return (host.isBlank() ? httpServer.rxListen(port) : httpServer.rxListen(port, host))
                                    .ignoreElement();
                        }
                ));
    }

    private void addActionToRouter(com.dburyak.vertx.core.deployment.spec.HttpProxyInVerticle.OutAction action) {
        httpRouter.route()
                .method(action.getMethod())
                .path(action.getFullPath(verticleSpec))
                .handler(routingCtx -> {
                    // TODO: implement - use call dispatcher here
                    throw new UnsupportedOperationException("not implemented");
                });
    }

    public static class Producer extends VerticleProducer<Producer> {
        private final com.dburyak.vertx.core.deployment.spec.HttpProxyInVerticle verticleSpec;

        public Producer(Verticles verticles, com.dburyak.vertx.core.deployment.spec.HttpProxyInVerticle verticleSpec) {
            this.verticleSpec = requireNonNull(verticleSpec);
            var numInstances = verticleSpec.getInstances() > 0 ? verticleSpec.getInstances() : verticles.getInstances();
            if (numInstances <= 0) {
                throw new IllegalArgumentException("num instances must be positive: numInstances=" + numInstances
                        + ", verticleName=" + verticleSpec.getName());
            }
            getDeploymentOptions().setInstances(numInstances);
            setName(verticleSpec.getName());
        }

        @Override
        protected DiVerticle doCreateVerticle() {
            var newVerticle = new HttpProxyInVerticle();
            newVerticle.setVerticleSpec(verticleSpec);
            return newVerticle;
        }
    }
}
