package com.archiuse.mindis.test.integration

import com.archiuse.mindis.MindisVerticle
import io.micronaut.context.ApplicationContext

class IntegrationTestVerticle extends MindisVerticle {
    ApplicationContext getVerticleBeanCtx() {
        super.verticleBeanCtx
    }
}
