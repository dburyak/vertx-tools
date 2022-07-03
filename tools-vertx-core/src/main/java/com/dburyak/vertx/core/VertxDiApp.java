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
import java.util.Collection;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Base class for DI enabled vertx application.
 * <p>
 * Unlike classic vertx application where all the initialization needs to be done imperatively, this class does it
 * in declarative manner. To use it declare verticles by inheriting from {@link DiVerticle} and specify which verticles
 * to deploy in this application.
 */
@Slf4j
public abstract class VertxDiApp {
    private final Object startupLock = new Object();
    private volatile ApplicationContext appCtx = null;

    protected abstract Collection<VerticleDeploymentDescriptor> verticlesDeploymentDescriptors();

    public final Completable start() {
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
                                                    log.error("verticle must have public no-args constructor");
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
                        log.info("verticle deployed: depId={}, verticle={}", depInfo.getKey(), depInfo.getValue()))
                )
                .ignoreElements()
                .doOnComplete(() -> log.info("all verticles deployed"))
                .doOnComplete(() -> log.info("vertx application started"));
    }

    public final Completable stop() {
        return Completable.defer(() -> {
            synchronized (startupLock) {
                if (appCtx == null) {
                    log.debug("attempt to stop vertx application that is already stopped");
                    return Completable.complete();
                }
                var vertx = appCtx.getBean(Vertx.class);
                return vertx.close()
                        .doOnSubscribe(ignr -> log.info("stopping vertx application"))
                        .andThen(Completable.fromRunnable(() -> {
                            synchronized (startupLock) {
                                appCtx.stop();
                                appCtx = null;
                            }
                        }));
            }
        });
    }
}
