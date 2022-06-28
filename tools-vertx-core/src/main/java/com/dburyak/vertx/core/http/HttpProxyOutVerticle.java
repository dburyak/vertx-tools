package com.dburyak.vertx.core.http;

import com.dburyak.vertx.core.DiVerticle;
import com.dburyak.vertx.core.VerticleProducer;
import io.reactivex.Completable;

import javax.inject.Singleton;

/**
 * Proxy verticle to send calls to other verticles via http.
 *
 * Not designed to be used for public API. Designed to be used only when there's a need to replace verticle-to-verticle
 * communication via EventBus with communication over http. Should be used only in cases when rearranging verticles in
 * deployments and it is no longer possible for two verticles to communicate directly via EventBus.
 * <p>
 * For developing public APIs (or stable private APIs) use default vertx facilities for rest api development.
 */
@Singleton
public class HttpProxyOutVerticle extends DiVerticle {

    @Override
    protected Completable doOnStart() {
        return super.doOnStart();
    }

    @Override
    protected Completable doOnStop() {
        return super.doOnStop();
    }

    public static class Producer extends VerticleProducer<Producer> {

        @Override
        protected DiVerticle doCreateVerticle() {
            return new HttpProxyOutVerticle();
        }
    }
}
