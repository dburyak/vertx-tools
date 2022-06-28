package com.dburyak.vertx.auth;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Secondary;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import lombok.Setter;

import javax.inject.Singleton;

@Factory
@Secondary
public class JwtAuthFactory {

    @Setter(onParam_ = {@Property(name = "jwt.private-key")})
    private String jwtPrivateKey;

    @Setter(onParam_ = {@Property(name = "jwt.algorithm")})
    private String jwtAlgorithm;

    @Property(name = "jwt.is-symmetric")
    private boolean isSymmetric;

    @Property(name = "jwt.issuer")
    private String issuer;

    @Singleton
    @Secondary
    public JWTAuth jwtAuth(Vertx vertx, JWTAuthOptions jwtAuthOptions) {
        return JWTAuth.create(vertx, jwtAuthOptions);
    }

    @Prototype
    @Secondary
    public JWTAuthOptions jwtAuthOptions(PubSecKeyOptions pubSecKeyOptions) {
        return new JWTAuthOptions().addPubSecKey(pubSecKeyOptions);
    }

    @Prototype
    @Secondary
    public JWTOptions jwtOptions() {
        return new JWTOptions().setIssuer(issuer);
    }

    @Singleton
    @Secondary
    public JWTAuthHandler jwtAuthRouterHandler(JWTAuth jwtAuth) {
        return JWTAuthHandler.create(jwtAuth);
    }
}
