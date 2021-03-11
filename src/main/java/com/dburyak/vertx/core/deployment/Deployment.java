package com.dburyak.vertx.core.deployment;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class Deployment {
    private List<Verticle> verticles;
}
