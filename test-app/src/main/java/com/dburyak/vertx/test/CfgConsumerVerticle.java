package com.dburyak.vertx.test;

import com.dburyak.vertx.core.AbstractDiVerticle;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.config.ConfigRetriever;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Bean
@Slf4j
public class CfgConsumerVerticle extends AbstractDiVerticle {

    @Inject
    private ConfigRetriever cfgRetriever;

    @Override
    public Completable startup() {
        return cfgRetriever.rxGetConfig()
                .doOnSuccess(cfg -> log.info("initial config: {}", cfg))
                .ignoreElement().andThen(Completable.fromRunnable(() -> {
                            cfgRetriever.listen(cfgChange -> {
                                log.info("config changed: prev={}, next={}", cfgChange.getPreviousConfiguration(),
                                        cfgChange.getNewConfiguration());
                            });
                        })
                        .doOnComplete(() -> log.info("started cfg consumer verticle")));
    }
}
