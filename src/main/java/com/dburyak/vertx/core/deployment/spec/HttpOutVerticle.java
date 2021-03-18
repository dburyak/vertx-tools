package com.dburyak.vertx.core.deployment.spec;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class HttpOutVerticle extends Verticle {
    private static final String BASE_ADDR_DEFAULT = "";
    private static final Headers HEADERS_DEFAULT = Headers.all();
    private static final Auth AUTH_DEFAULT = Auth.disabled();
    private static final List<Path> PATHS_DEFAULT = Collections.emptyList();

    private final String baseAddr;
    private final String url;
    private final Headers headers;
    private final Auth auth;
    private final List<Path> paths;

    @Override
    public List<String> getAllAddresses() {
        return paths.stream()
                .map(p -> p.getFullAddr(this))
                .collect(Collectors.toList());
    }

    protected HttpOutVerticle(HttpOutVerticleBuilder<?, ?> builder) {
        super(builder);
        baseAddr = (builder.baseAddr != null) ? builder.baseAddr.strip() : BASE_ADDR_DEFAULT;
        if (builder.url == null || builder.url.isBlank()) {
            throw new IllegalStateException("url must be specified for http_out verticle: name=" + getName());
        }
        url = builder.url.strip();
        headers = (builder.headers != null) ? builder.headers : HEADERS_DEFAULT;
        auth = (builder.auth != null) ? builder.auth : AUTH_DEFAULT;
        paths = (builder.paths != null) ? builder.paths : PATHS_DEFAULT;
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
    @Builder(toBuilder = true)
    public static class Path {
        private final String path;
        private final HttpMethod method;
        private final String url;
        private final Headers headers;
        private final Auth auth;
        private final String baseAddr;
        private final String addr;

        public String getFullAddr(HttpOutVerticle parentVerticle) {
            return ((baseAddr != null) ? baseAddr : parentVerticle.baseAddr) + addr;
        }

        public static class PathBuilder {
            public Path build() {
                if (path == null || path.isBlank()) {
                    throw new IllegalStateException("path must be specified");
                }
                var effectivePath = path.strip();
                Objects.requireNonNull(method);
                var effectiveUrl = (url != null) ? url.strip() : null;
                var effectiveBaseAddr = (baseAddr != null) ? baseAddr.strip() : null;
                var effectiveAddr = (addr != null && !addr.isBlank()) ? addr.strip()
                        : method.name() + ":" + effectivePath;
                return new Path(effectivePath, method, effectiveUrl, headers, auth, effectiveBaseAddr, effectiveAddr);
            }
        }
    }
}
