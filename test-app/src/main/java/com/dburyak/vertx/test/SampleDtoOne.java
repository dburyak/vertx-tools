package com.dburyak.vertx.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SampleDtoOne {
    private String strValue;
    private int intValue;

    public SampleDtoOne(SampleDtoOne from) {
        this.strValue = from.strValue;
        this.intValue = from.intValue;
    }
}
