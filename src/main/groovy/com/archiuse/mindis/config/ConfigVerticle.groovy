package com.archiuse.mindis.config

import com.archiuse.mindis.MindisVerticle
import groovy.util.logging.Slf4j
import io.reactivex.Completable

@Slf4j
class ConfigVerticle extends MindisVerticle {

    @Override
    Completable rxStart() {
        Completable
                .fromAction {}
                .doOnSubscribe { log.info 'starting config verticle: {}', this }
                .doOnComplete { log.info 'config verticle started: {}', this }
    }

    @Override
    Completable rxStop() {
        Completable
                .fromAction {}
                .doOnSubscribe { log.info 'stopping config verticle: {}', this }
                .doOnComplete { log.info 'config verticle stopped: {}', this }
    }
}
