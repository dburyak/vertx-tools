package com.dburyak.vertx.core.util;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;

import javax.inject.Singleton;
import java.util.Base64;

@Factory
@Secondary
public class Base64Factory {

    @Singleton
    @Secondary
    public Base64.Decoder base64Decoder() {
        return Base64.getUrlDecoder();
    }

    @Singleton
    @Secondary
    public Base64.Encoder base64Encoder() {
        return Base64.getUrlEncoder();
    }
}
