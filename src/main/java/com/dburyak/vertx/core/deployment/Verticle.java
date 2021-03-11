package com.dburyak.vertx.core.deployment;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class Verticle {
    private String action;
    private CommunicationType communicationType;
    private CommunicationProtocol communicationProtocol;
    private String addr;
}
