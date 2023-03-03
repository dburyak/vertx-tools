package com.dburyak.vertx.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SampleDtoTwo {
    private String strValue;
    private int intValue;

    public SampleDtoTwo(SampleDtoTwo from) {
        this.strValue = from.strValue;
        this.intValue = from.intValue;
    }
}
