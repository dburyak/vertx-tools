package com.dburyak.vertx.core.call;

public interface RoutingConfigParser {
    boolean canParse(RoutingConfigFormat format);

    RoutingConfigFormat parse(Object encodedRoutingConfig);
}
