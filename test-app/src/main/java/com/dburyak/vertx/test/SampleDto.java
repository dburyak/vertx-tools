package com.dburyak.vertx.test;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SampleDto {
    private String strValue;
    private int intValue;
}
