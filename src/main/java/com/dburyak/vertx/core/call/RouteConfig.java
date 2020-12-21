package com.dburyak.vertx.core.call;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RouteConfig {
    private String actionId;
    private CommunicationType communicationType;
    private CallType callType;
    private String ebAddress;
}
