package com.dburyak.vertx.test.async;

import com.dburyak.vertx.core.AsyncInitializable;
import com.dburyak.vertx.core.di.VerticleScope;
import com.dburyak.vertx.core.di.VerticleStartup;
import com.dburyak.vertx.test.HelloVerticle2;
import io.micronaut.context.annotation.Bean;
import io.reactivex.rxjava3.core.Completable;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Bean
@VerticleStartup(HelloVerticle2.class)
@VerticleScope
@Log4j2
public class AsyncUnrelatedVerticleStartup implements AsyncInitializable {

    @PostConstruct
    public void initSync() {
        log.info("AsyncUnrelatedVerticleStartup sync init - SHOULD NOT BE CALLED for AsyncLifecycleBeansApp");
    }

    @Override
    public Completable initAsync() {
        return Completable.fromRunnable(() ->
                log.info("AsyncUnrelatedVerticleStartup async init - SHOULD NOT BE CALLED for AsyncLifecycleBeansApp"))
                .delay(3_000, MILLISECONDS);
    }
}
