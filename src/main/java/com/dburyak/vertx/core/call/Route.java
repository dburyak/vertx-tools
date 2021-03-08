package com.dburyak.vertx.core.call;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class Route {
    private String actionId;
    private CommunicationProtocol communicationProtocol;
    private CallType callType;
    private String ebAddress;
}
