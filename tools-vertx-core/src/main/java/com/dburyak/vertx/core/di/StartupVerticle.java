package com.dburyak.vertx.core.di;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier to mark verticles that should be deployed on application start. Applicable to
 * {@link com.dburyak.vertx.core.VerticleDeploymentDescriptor}.
 *
 * <p>Such verticles should be considered as the entry point of micronaut-di vertx application. I.e. such verticles are
 * supposed to deploy all the other verticles that comprise the application or sub-system of the application
 * (e.g. application main business logic, metrics, intra-application verticles monitoring and self-healing, etc.).
 */
@Qualifier
@Retention(RUNTIME)
@Documented
public @interface StartupVerticle {
}
