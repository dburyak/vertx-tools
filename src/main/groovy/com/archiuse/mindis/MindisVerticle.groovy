package com.archiuse.mindis

import com.archiuse.mindis.di.Vertx
import io.vertx.reactivex.core.AbstractVerticle

import javax.inject.Singleton

@Singleton
@Vertx
abstract class MindisVerticle extends AbstractVerticle {
}
