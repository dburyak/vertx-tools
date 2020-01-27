package com.archiuse.mindis.test.integration

import com.archiuse.mindis.MindisVertxApplication
import com.archiuse.mindis.VerticleProducer
import groovy.util.logging.Slf4j
import io.reactivex.Completable

import static com.archiuse.mindis.app.AppState.RUNNING
import static com.archiuse.mindis.app.AppState.STOPPED

@Slf4j
class VertxIntegrationApp extends MindisVertxApplication {

    static VertxIntegrationApp getInstance() {
        InstanceHolder.INSTANCE
    }

    static Completable startIfNotRunning() {
        if (instance.appState == STOPPED) {
            log.info 'starting mindis integration test application before tests'
            instance.start()
        } else {
            Completable.complete()
        }
    }

    static Completable stopIfRunning() {
        if (instance.appState == RUNNING) {
            log.info 'stopping mindis integration test application after tests'
            instance.stop()
        } else {
            Completable.complete()
        }
    }

    @Override
    List<VerticleProducer> getVerticlesProducers() {
        []
    }

    private static final class InstanceHolder {
        static final VertxIntegrationApp INSTANCE = new VertxIntegrationApp()
    }

    private VertxIntegrationApp() {
    }
}
