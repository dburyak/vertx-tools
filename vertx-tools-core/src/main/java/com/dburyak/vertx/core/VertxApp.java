package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.AppBootstrap;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.DeploymentOptions;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Base class for DI enabled vertx application.
 * <p>
 * Unlike classic vertx application where all the initialization needs to be done imperatively, this class does it in
 * declarative manner. To use it declare verticles by inheriting from {@link AbstractDiVerticle} and specify which
 * verticles to deploy in this application using {@link VerticleDeploymentDescriptor}.
 */
@Slf4j
public abstract class VertxApp {
    private final Object startupLock = new Object();
    private volatile ApplicationContext appCtx = null;

    /**
     * Specify verticles to deploy in this application.
     *
     * @return collection of verticle deployment descriptors
     */
    protected abstract Collection<VerticleDeploymentDescriptor> verticlesDeploymentDescriptors();

    /**
     * Start DI enabled vertx application.
     *
     * @return completable that completes when application is started
     */
    public final Completable start() {
        var invokedAt = Instant.now();
        return Observable.defer(() -> {
                    synchronized (startupLock) {
                        if (appCtx != null) {
                            log.debug("attempt to start vertx application that is already running");
                            return Observable.empty();
                        }
                        log.info("starting vertx application");
                        appCtx = ApplicationContext.run();
                        var vertx = appCtx.getBean(Vertx.class);
                        log.info("initialization");
                        appCtx.getBeansOfType(Object.class, Qualifiers.byStereotype(AppBootstrap.class));
                        var deployments = verticlesDeploymentDescriptors().stream()
                                .flatMap(d -> {
                                    var verticleDeploymentOpts = d.getDeploymentOptions();
                                    return IntStream.range(0, verticleDeploymentOpts.getInstances())
                                            .mapToObj(ignr -> {
                                                try {
                                                    var verticleInstance = d.getVerticleClass()
                                                            .getDeclaredConstructor().newInstance();
                                                    verticleInstance.setAppCtx(appCtx);
                                                    var modOpts = new DeploymentOptions(d.getDeploymentOptions())
                                                            .setInstances(1);
                                                    return Map.entry(verticleInstance, d.toBuilder()
                                                            .deploymentOptions(modOpts)
                                                            .build()
                                                    );
                                                } catch (InstantiationException | IllegalAccessException |
                                                         NoSuchMethodException e) {
                                                    log.error("verticle must have public no-args constructor: {}",
                                                            d.getVerticleClass());
                                                    throw new RuntimeException(e);
                                                } catch (InvocationTargetException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            });
                                })
                                .map(e -> vertx.rxDeployVerticle(e.getKey(), e.getValue().getDeploymentOptions())
                                        .map(depId -> Map.entry(depId, e.getKey())))
                                .toList();
                        return Observable.fromIterable(deployments)
                                .doOnSubscribe(ignr -> log.info("deploy verticles"));
                    }
                })
                .flatMapSingle(d -> d.doOnSuccess(depInfo ->
                        log.debug("verticle deployed: depId={}, verticle={}", depInfo.getKey(), depInfo.getValue()))
                )
                .ignoreElements()
                .doOnComplete(() -> log.info("verticles deployed"))
                .doOnComplete(() -> log.info("vertx application started: time={}",
                        Duration.between(invokedAt, Instant.now())));
    }

    /**
     * Stop DI enabled vertx application.
     *
     * @return completable that completes when application is stopped
     */
    public final Completable stop() {
        return Completable.defer(() -> {
            synchronized (startupLock) {
                if (appCtx == null) {
                    log.debug("attempt to stop vertx application that is already stopped");
                    return Completable.complete();
                }
                var vertx = appCtx.getBean(Vertx.class);
                return vertx.rxClose()
                        .doOnSubscribe(ignr -> log.info("stopping vertx application"))
                        .doOnComplete(() -> log.info("vertx stopped"))
                        .andThen(Completable.fromRunnable(() -> {
                            synchronized (startupLock) {
                                appCtx.stop();
                                appCtx = null;
                            }
                            log.info("beans disposed");
                        }))
                        .doOnComplete(() -> log.info("vertx application stopped"));
            }
        });
    }
}
