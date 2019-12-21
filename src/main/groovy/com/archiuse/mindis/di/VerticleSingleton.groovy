package com.archiuse.mindis.di

import javax.inject.Singleton
import java.lang.annotation.Retention

import static java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * Verticle local singleton bean scope.
 */
@Singleton
@Vertx
@Retention(RUNTIME)
@interface VerticleSingleton {
}
