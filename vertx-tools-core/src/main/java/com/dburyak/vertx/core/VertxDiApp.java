package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.AppBootstrap;
import com.dburyak.vertx.core.di.AppStartup;
import com.dburyak.vertx.core.di.ForEventLoop;
import com.dburyak.vertx.core.di.VertxThreadScopeBase;
import com.dburyak.vertx.core.util.Tuple;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static java.util.Collections.synchronizedList;

/**
 * Base class for DI enabled vertx application.
 * <p>
 * Unlike classic vertx application where all the initialization needs to be done imperatively, this class does it in
 * declarative manner. To use it declare verticles by inheriting from {@link AbstractDiVerticle} and specify which
 * verticles to deploy in this application using {@link VerticleDeploymentDescriptor}.
 */
@Slf4j
public abstract class VertxDiApp {
    private final Object startupLock = new Object();
    private final List<String> verticlesDeploymentIds = synchronizedList(new ArrayList<>());
    private volatile ApplicationContext appCtx = null;

    /**
     * Specify verticles to deploy in this application.
     *
     * @return collection of verticle deployment descriptors
     */
    protected abstract Collection<VerticleDeploymentDescriptor> verticlesDeploymentDescriptors();

    /**
     * Application ctx configurers to tweak DI application context before it is started. Is useful to control which
     * config files to load, which config mechanisms to enable/disable, etc. By default, returns an empty list.
     * Implementations can override this method to provide custom configurers.
     *
     * @return list of application context configurers
     */
    protected List<AppCtxConfigurer> appCtxConfigurers() {
        return List.of();
    }

    /**
     * Start DI enabled vertx application.
     *
     * @return completable that completes when application is started
     */
    public final Completable start() {
        var invokedAt = new AtomicReference<Instant>();
        return Observable.defer(() -> {
                    synchronized (startupLock) {
                        invokedAt.set(Instant.now());
                        if (appCtx != null) {
                            log.debug("attempt to start vertx application that is already running");
                            return Observable.empty();
                        }
                        log.info("starting vertx application");
                        var appCtxBuilder = ApplicationContext.builder();
                        for (var appCtxConfigurer : appCtxConfigurers()) {
                            appCtxBuilder = appCtxConfigurer.configure(appCtxBuilder);
                        }
                        appCtx = appCtxBuilder.build().start();
                        var vertx = appCtx.getBean(Vertx.class);
                        log.info("bootstrap phase");
                        appCtx.getBeansOfType(Object.class, Qualifiers.byStereotype(AppBootstrap.class));
                        log.info("startup phase");
                        var elScheduler = appCtx.getBean(Scheduler.class, Qualifiers.byStereotype(ForEventLoop.class));
                        Single.fromCallable(() -> appCtx.getBeansOfType(Object.class,
                                        Qualifiers.byStereotype(AppStartup.class)))
                                .flatMapCompletable(startupBeans ->
                                        Completable.merge(startupBeans.stream()
                                                .filter(AsyncInitializable.class::isInstance)
                                                .map(AsyncInitializable.class::cast)
                                                .map(AsyncInitializable::initAsync)
                                                .toList()))
                                .subscribeOn(elScheduler)
                                .blockingAwait(); // blocks "main" thread, not the EL
                        var deployments = verticlesDeploymentDescriptors().stream()
                                .flatMap(d -> {
                                    var verticleDeploymentOpts = d.getDeploymentOptions();
                                    return IntStream.range(0, verticleDeploymentOpts.getInstances())
                                            .mapToObj(ignr -> {
                                                try {
                                                    var verticleInstance = d.getVerticleClass()
                                                            .getDeclaredConstructor().newInstance();
                                                    verticleInstance.setAppCtx(appCtx);
                                                    verticleInstance.setVertx(vertx);
                                                    var modOpts = new DeploymentOptions(d.getDeploymentOptions())
                                                            .setInstances(1);
                                                    return Tuple.of(verticleInstance, d.toBuilder()
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
                                .map(t2 -> vertx.rxDeployVerticle(t2.getV1(), t2.getV2().getDeploymentOptions())
                                        .map(depId -> Tuple.of(depId, t2.getV1())))
                                .toList();
                        return Observable.fromIterable(deployments)
                                .doOnSubscribe(ignr -> log.info("deploy verticles"));
                    }
                })
                .flatMapSingle(d -> d.doOnSuccess(depInfo -> {
                    log.debug("verticle deployed: depId={}, verticle={}", depInfo.getV1(), depInfo.getV2());
                    verticlesDeploymentIds.add(depInfo.getV1());
                }))
                .ignoreElements()
                .doOnComplete(() -> {
                    log.info("verticles deployed");
                    log.info("vertx application started: time={}", Duration.between(invokedAt.get(), Instant.now()));
                });
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
                var invokedAt = Instant.now();
                var verticlesUndeployStartedAt = new AtomicReference<Instant>();
                var vertxShutdownStartedAt = new AtomicReference<Instant>();
                var beansClosingStartedAt = new AtomicReference<Instant>();
                var vertx = appCtx.getBean(Vertx.class);
                return Observable.fromIterable(verticlesDeploymentIds)
                        .doOnSubscribe(ignr -> {
                            log.info("stopping vertx application");
                            log.info("undeploying verticles: numVerticles={}", verticlesDeploymentIds.size());
                            verticlesUndeployStartedAt.set(Instant.now());
                        })
                        .flatMapCompletable(depId -> vertx.rxUndeploy(depId)
                                .doOnComplete(() -> log.debug("verticle undeployed: depId={}", depId)))
                        .doOnComplete(() -> {
                            log.info("all verticles undeployed: time={}",
                                    Duration.between(verticlesUndeployStartedAt.get(), Instant.now()));
                            verticlesDeploymentIds.clear();
                        })
                        .andThen(Completable.defer(() -> {
                            synchronized (startupLock) {
                                var vertxThreadScopes = appCtx.getBeansOfType(VertxThreadScopeBase.class);
                                var asyncCloseableSingletons = appCtx.getActiveBeanRegistrations(AsyncCloseable.class);
                                return Observable.fromIterable(vertxThreadScopes)
                                        .flatMapCompletable(VertxThreadScopeBase::stopAsync)
                                        .mergeWith(Observable.fromIterable(asyncCloseableSingletons)
                                                .map(BeanRegistration::getBean)
                                                .cast(AsyncCloseable.class)
                                                .flatMapCompletable(AsyncCloseable::closeAsync))
                                        .doOnSubscribe(ignr -> {
                                            beansClosingStartedAt.set(Instant.now());
                                            log.info("disposing beans");
                                        });
                            }
                        }))
                        .andThen(Completable.fromRunnable(() -> {
                            synchronized (startupLock) {
                                appCtx.stop();
                                appCtx = null;
                            }
                            log.info("beans disposed: time={}",
                                    Duration.between(beansClosingStartedAt.get(), Instant.now()));
                        }))
                        .andThen(vertx.rxClose().doOnSubscribe(ignr -> {
                            log.debug("closing vertx instance");
                            vertxShutdownStartedAt.set(Instant.now());
                        }))
                        .doOnComplete(() -> {
                            log.info("vertx instance closed: time={}",
                                    Duration.between(vertxShutdownStartedAt.get(), Instant.now()));
                            log.info("vertx application stopped: time={}", Duration.between(invokedAt, Instant.now()));
                        });
            }
        });
    }

    /**
     * Get bean of specified type from the underlying DI container. This method is expected to be used only for
     * singleton beans.
     *
     * @param beanType type of bean to get
     * @param <T> type of bean
     *
     * @return bean instance
     */
    public <T> T getBean(Class<T> beanType) {
        return appCtx.getBean(beanType);
    }
}
