package com.dburyak.vertx.core.deployment.spec;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class HttpProxyOutVerticle extends Verticle<HttpProxyOutVerticle.InAction, HttpProxyOutVerticle.OutAction> {
    private static final String BASE_ADDR_DEFAULT = "";
    private static final Headers HEADERS_DEFAULT = Headers.all();
    private static final Auth AUTH_DEFAULT = Auth.disabled();

    private final String baseAddr;
    private final String url;
    private final Headers headers;
    private final Auth auth;

    @Override
    public List<String> getAllAddresses() {
        return getInActions().getList().stream()
                .map(a -> a.getFullAddr(getInActions()))
                .collect(Collectors.toList());
    }

    protected HttpProxyOutVerticle(HttpProxyOutVerticleBuilder<?, ?> builder) {
        super(builder);
        baseAddr = (builder.baseAddr != null) ? builder.baseAddr.strip() : BASE_ADDR_DEFAULT;
        if (builder.url == null || builder.url.isBlank()) {
            throw new IllegalStateException("url must be specified for http_out verticle: name=" + getName());
        }
        url = builder.url.strip();
        headers = (builder.headers != null) ? builder.headers : HEADERS_DEFAULT;
        auth = (builder.auth != null) ? builder.auth : AUTH_DEFAULT;
    }

    @Data
    @Builder(toBuilder = true)
    public static class Auth {

        @Builder.Default
        private final boolean enabled = false;

        @Builder.Default
        private final CharSequence header = HttpHeaders.AUTHORIZATION;

        public static Auth disabled() {
            return Auth.builder()
                    .enabled(false)
                    .build();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    public static class InAction extends com.dburyak.vertx.core.deployment.spec.InAction {
        private final String path;
        private final HttpMethod method;
        private final String url;
        private final Headers headers;
        private final Auth auth;

        public String getFullUrl(HttpProxyOutVerticle verticle) {
            return ((url != null) ? url : verticle.url) + path;
        }

        protected InAction(InActionBuilder<?, ?> builder) {
            super(builder);
            path = (builder.path != null && !builder.path.isBlank()) ? builder.path.strip() : getName();
            method = requireNonNull(builder.method);
            url = (builder.url != null) ? builder.url.strip() : null;
            headers = builder.headers;
            auth = builder.auth;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    public static class OutAction extends com.dburyak.vertx.core.deployment.spec.OutAction {

        protected OutAction(OutActionBuilder<?, ?> builder) {
            super(builder);
            throw new UnsupportedOperationException("http_out verticle can not have out actions");
        }
    }
}
