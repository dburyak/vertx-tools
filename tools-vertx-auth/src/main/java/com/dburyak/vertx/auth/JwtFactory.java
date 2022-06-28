package com.dburyak.vertx.auth;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Secondary;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.impl.jose.JWK;
import io.vertx.ext.auth.impl.jose.JWT;
import lombok.Setter;

import javax.inject.Singleton;
import java.security.NoSuchAlgorithmException;

@Factory
@Secondary
public class JwtFactory {

    @Setter(onParam_ = {@Property(name = "jwt.private-key")})
    private String jwtPrivateKey;

    @Setter(onParam_ = {@Property(name = "jwt.algorithm")})
    private String jwtAlgorithm;

    @Setter(onParam_ = {@Property(name = "jwt.is-symmetric")})
    private boolean isSymmetric;

    @Singleton
    @Secondary
    public JWT jwt(JWK jwk) {
        return new JWT().addJWK(jwk);
    }

    // TODO: seems that JWK should not be Singleton at all. JWK - is just one of many registered ways of
    //  sign or/and verify tokens. For RSA there even must be at least two JWKs (one for signing, and one for
    //  verification). For key rotation we'll be adding new JWKs always, and replacing old ones with always failing
    //  JWKs - just ones with some random unknown key, in order to invalidate them. Unfortunately, there's no way
    //  to remove JWK after you have registered it, only replace with new version with same "kid"
    @Singleton
    @Secondary
    public JWK jwk(PubSecKeyOptions pubSecKeyOptions) throws NoSuchAlgorithmException {
        return new JWK(pubSecKeyOptions);
    }

    @Singleton
    @Secondary
    public PubSecKeyOptions pubSecKeyOptions() {
        if (!isSymmetric) {
            throw new AssertionError("asymmetric keys are not implemented ... yet");
        }
        return new PubSecKeyOptions()
                .setAlgorithm(jwtAlgorithm)
                .setBuffer(jwtPrivateKey);
    }
}
