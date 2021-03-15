package com.dburyak.vertx.core.deployment.spec;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class HttpOutVerticle extends Verticle {
    private final String url;

    @Builder.Default
    private Headers headers = Headers.all();

    @Builder.Default
    private Auth auth = Auth.builder().build();

    @Builder.Default
    private List<Path> paths = new ArrayList<>();

    protected HttpOutVerticle(HttpOutVerticleBuilder<?, ?> builder) {
        super(builder);
        url = builder.url;
        headers = builder.headers$value;
        auth = builder.auth$value;
        if (url == null || url.isBlank()) {
            // if common url is not specified, each path must have explicit url
            var pathsWithNoExplicitUrl = builder.paths$value.stream()
                    .filter(p -> p.getUrl() == null || p.getUrl().isBlank())
                    .collect(Collectors.toList());
            if (!pathsWithNoExplicitUrl.isEmpty()) {
                throw new IllegalStateException("no url specified for paths: " + pathsWithNoExplicitUrl);
            }
        }
        paths = builder.paths$value;
    }

    @Data
    @Builder(toBuilder = true)
    public static class Auth {

        @Builder.Default
        private final boolean enabled = false;

        @Builder.Default
        private final CharSequence header = HttpHeaders.AUTHORIZATION;
    }

    @Data
    @Builder(toBuilder = true)
    public static class Path {
        private final String path;
        private final HttpMethod method;
        private final String url;
        private final Headers headers;
        private final Auth auth;
        private final String addr;

        public static class PathBuilder {
            public Path build() {
                if (path == null || path.isBlank()) {
                    throw new IllegalStateException("path must be specified");
                }
                Objects.requireNonNull(method);
                if (headers == null) {
                    headers = Headers.all();
                }
                if (addr == null || addr.isBlank()) {
                    addr = method.name() + ":" + path;
                }
                return new Path(path, method, url, headers, auth, addr);
            }
        }
    }
}
