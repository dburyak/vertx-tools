package com.archiuse.mindis.di

import javax.inject.Qualifier
import java.lang.annotation.Retention

import static java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * Vertx specific bean implementation.
 */
@Qualifier
@Retention(RUNTIME)
@interface Vertx {
}
