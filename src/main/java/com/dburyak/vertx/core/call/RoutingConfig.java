package com.dburyak.vertx.core.call;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class RoutingConfig {
    private List<RouteConfig> routes;
}
