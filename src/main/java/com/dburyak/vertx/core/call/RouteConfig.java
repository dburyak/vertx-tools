package com.dburyak.vertx.core.call;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class RouteConfig {
    private String actionId;
    private CommunicationType communicationType;
    private CallType callType;
    private String ebAddress;
}
