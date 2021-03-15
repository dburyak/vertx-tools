package com.dburyak.vertx.core.deployment.spec;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class HttpInVerticle extends Verticle {
    private static final String BASE_PATH_DEFAULT = "";
    private static final Headers HEADERS_DEFAULT = Headers.all();
    private static final Auth AUTH_DEFAULT = Auth.disabled();
    private static final List<Path> PATHS_DEFAULT = Collections.emptyList();

    private final String basePath;
    private final Headers headers;
    private final Auth auth;
    private final List<Path> paths;

    protected HttpInVerticle(HttpInVerticleBuilder<?, ?> builder) {
        super(builder);
        basePath = (builder.basePath != null) ? builder.basePath.strip() : BASE_PATH_DEFAULT;
        headers = (builder.headers != null) ? builder.headers : HEADERS_DEFAULT;
        auth = (builder.auth != null) ? builder.auth : AUTH_DEFAULT;
        paths = (builder.paths != null) ? builder.paths : PATHS_DEFAULT;
    }

    @Override
    public List<String> getAllAddresses() {
        // http in verticle doesn't listen on any address, only sends
        return Collections.emptyList();
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
    @Builder(toBuilder = true)
    public static class Path {
        private final String name;
        private final String path;
        private final String basePath;
        private final HttpMethod method;
        private final String action;
        private final Auth auth;
        private final Headers headers;

        public String getFullPath(HttpInVerticle verticle) {
            return ((basePath != null) ? basePath : verticle.basePath) + path;
        }

        public static class PathBuilder {
            public Path build() {
                if (name == null || name.isBlank()) {
                    throw new IllegalStateException("http_in verticle path name must be specified: method="
                            + method + ", path=" + path);
                }
                var effectiveName = name.strip();
                var effectivePath = (path != null) ? path : name;
                var effectiveBasePath = (basePath != null) ? basePath.strip() : null;
                if (method == null) {
                    throw new IllegalStateException("http_in verticle method must be specified: path=" + path);
                }
                var effectiveAction = (action != null && !action.isBlank()) ? action.strip() : effectiveName;
                return new Path(effectiveName, effectivePath.strip(), effectiveBasePath, method, effectiveAction, auth,
                        headers);
            }
        }
    }
}
