package com.dburyak.vertx.core.deployment.spec;

import com.dburyak.vertx.core.VerticleProducer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class HttpProxyInVerticle extends Verticle<HttpProxyInVerticle.InAction, HttpProxyInVerticle.OutAction> {
    private static final String BASE_PATH_DEFAULT = "";
    private static final Headers HEADERS_DEFAULT = Headers.all();
    private static final Auth AUTH_DEFAULT = Auth.disabled();

    private final String basePath;
    private final Headers headers;
    private final Auth auth;
    private final int port;

    protected HttpProxyInVerticle(HttpProxyInVerticleBuilder<?, ?> builder) {
        super(builder);
        if (!getInActions().getList().isEmpty()) {
            // double check to be sure
            throw new IllegalStateException("http_in verticle can not have input actions");
        }
        if (getOutActions().getList().isEmpty()) {
            throw new IllegalStateException("http_in verticle must have out actions (path specs)");
        }
        basePath = (builder.basePath != null) ? builder.basePath.strip() : BASE_PATH_DEFAULT;
        headers = (builder.headers != null) ? builder.headers : HEADERS_DEFAULT;
        auth = (builder.auth != null) ? builder.auth : AUTH_DEFAULT;
        port = builder.port;
    }

    @Override
    public List<String> getAllAddresses() {
        // http in verticle doesn't listen on any address, only sends
        return Collections.emptyList();
    }

    @Override
    public List<VerticleProducer<?>> createBySpec(Verticles verticles) {
        return List.of(new com.dburyak.vertx.core.http.HttpProxyInVerticle.Producer(verticles, this));
    }

    @Data
    @Builder(toBuilder = true)
    public static class Auth {

        @Builder.Default
        private final boolean enabled = false;

        @Builder.Default
        private final CharSequence header = HttpHeaders.AUTHORIZATION;

        @Builder.Default
        private final List<String> roles = Collections.emptyList();

        public static Auth disabled() {
            return Auth.builder()
                    .enabled(false)
                    .build();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    public static class OutAction extends com.dburyak.vertx.core.deployment.spec.OutAction {
        private final String basePath;
        private final String path;
        private final HttpMethod method;
        private final Auth auth;
        private final Headers headers;

        public String getFullPath(HttpProxyInVerticle verticle) {
            return ((basePath != null) ? basePath : verticle.basePath) + path;
        }

        protected OutAction(OutActionBuilder<?, ?> builder) {
            super(builder);
            basePath = (builder.basePath != null) ? builder.basePath.strip() : null;
            path = (builder.path != null) ? builder.path.strip() : getName();
            method = requireNonNull(builder.method);
            auth = builder.auth;
            headers = builder.headers;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    public static class InAction extends com.dburyak.vertx.core.deployment.spec.InAction {

        protected InAction(InActionBuilder<?, ?> builder) {
            super(builder);
            throw new UnsupportedOperationException("http_in verticle can not have input actions");
        }
    }
}
