package com.dburyak.vertx.core.call;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class RoutingConfig {

    /**
     * Key - action id, value - route config.
     */
    private Map<String, RouteConfig> routes;
}
