package com.archiuse.mindis

import groovy.util.logging.Slf4j

import static java.util.concurrent.TimeUnit.SECONDS

@Slf4j
class TestApp extends MindisVertxApplication {
    static {
        System.setProperty('vertx.logger-delegate-factory-class-name', 'io.vertx.core.logging.SLF4JLogDelegateFactory')
    }

    static void main(String[] args) {


        def app = new TestApp()
        app
                .start()
                .delay(10, SECONDS)
                .andThen(app.stop())
                .subscribe({
                    log.info 'startup sequence completed'
                }, {
                    log.error 'error on startup', it
                })
    }

    @Override
    List<VerticleProducer> getVerticlesProducers() {
        [{ new TestVerticle1() } as VerticleProducer,
         { new TestVerticle2() } as VerticleProducer]
    }
}
