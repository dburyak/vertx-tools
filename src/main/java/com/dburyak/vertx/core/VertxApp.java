package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.AppBean;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dburyak.vertx.core.AppState.FAILED;
import static com.dburyak.vertx.core.AppState.RUNNING;
import static com.dburyak.vertx.core.AppState.STARTING;
import static com.dburyak.vertx.core.AppState.STOPPED;
import static com.dburyak.vertx.core.AppState.STOPPING;

@Slf4j
public abstract class VertxApp {
    public static final String PROP_IS_APP_BEAN_CTX = "vertx.app.bean.ctx.main";

    private volatile ApplicationContext applicationBeanContext;
    private final Map<String, ApplicationContext> verticlesBeanContexts = new ConcurrentHashMap<>();
    private volatile AppState appState = STOPPED;

    public final Completable start() {
        return Completable
                .fromAction(() -> {
                    appState = STARTING;
                    var isRunning = applicationBeanContext != null;
                    if (isRunning) {
                        appState = FAILED;
                        throw new IllegalStateException("application is already running: appState=" + appState);
                    }
                })

                .andThen(startAppContext())
                .doOnSubscribe(ignr -> log.info("starting application"))
                .doOnSuccess(appCtx -> applicationBeanContext = appCtx)

                .flatMapCompletable(mainCtx -> Observable
                        .fromIterable(getVerticlesProducers())
                        .flatMap(this::deployVerticle)
                        .ignoreElements()) // discard deployment id, we no longer need it here

                .doOnComplete(() -> {
                    appState = RUNNING;
                    log.info("application started");
                })
                .doOnError(e -> {
                    appState = FAILED;
                    log.error("failed to start application", e);
                });
    }

    public final Completable stop() {
        return Single
                .fromCallable(() -> {
                    appState = STOPPING;
                    var mainCtx = applicationBeanContext;
                    var isRunning = (mainCtx != null);
                    if (!isRunning) {
                        appState = FAILED;
                        throw new IllegalStateException("application is not running: appState=" + appState);
                    }
                    return mainCtx;
                })

                // stop vertx
                .flatMap(mainCtx -> mainCtx.getBean(Vertx.class)
                        .rxClose()
                        .doOnSubscribe(ignr -> log.info("stopping application"))
                        .doOnComplete(() -> log.debug("vertx closed"))
                        .toSingleDefault(mainCtx))

                // stop bean context for each verticle
                .flatMapObservable(mainCtx -> Observable
                        .fromIterable(verticlesBeanContexts.entrySet())
                        .doOnSubscribe(ignr -> log.debug("stopping all verticle bean contexts"))
                        .doOnNext(e -> {
                            var depId = e.getKey();
                            var verticleCtx = e.getValue();
                            log.debug("closing verticle bean context: depId={}, verticleCtx={}",
                                    depId, verticleCtx);
                            verticleCtx.stop();
                            verticlesBeanContexts.remove(depId);
                            log.debug("verticle bean context closed: depId={}, verticleCtx={}", depId, verticleCtx);
                        })
                        .doOnComplete(() -> log.debug("all verticle bean contexts stopped"))

                        // stop main application bean context
                        .concatWith(Completable.fromAction(() -> {
                            log.debug("stopping application main bean context: appCtx={}", mainCtx);
                            mainCtx.stop();
                            applicationBeanContext = null;
                            log.debug("application main bean context stopped: appCtx={}", mainCtx);
                        })))

                .ignoreElements()
                .doOnComplete(() -> {
                    appState = STOPPED;
                    log.info("application stopped");
                })
                .doOnError(e -> {
                    appState = FAILED;
                    log.error("failed to stop application", e);
                });
    }

    public final Observable<String> deployVerticle(VerticleProducer<?> verticleProducer) {
        var num = verticleProducer.getDeploymentOptions().getInstances();
        var opts = num > 1
                ? new DeploymentOptions(verticleProducer.getDeploymentOptions()).setInstances(1)
                : verticleProducer.getDeploymentOptions();
        return Observable
                .range(0, num)
                .flatMapSingle(ignr -> Single
                        .fromCallable(() -> {
                            log.info("deploying micronaut verticle: verticleProducerName={}",
                                    verticleProducer.getName());
                            var mainCtx = applicationBeanContext;
                            if (mainCtx == null) {
                                log.error("can not deploy verticle, application is not running: appState={}", appState);
                                throw new IllegalStateException("can not deploy verticle, bad app state: " + appState);
                            }
                            log.debug("init bean context for verticle");
                            var verticleCtx = newApplicationContextBuilder()
                                    .properties(Map.of(PROP_IS_APP_BEAN_CTX, false))
                                    .start();
                            log.debug("inject main app beans into verticle bean context: mainCtx={}, verticleCtx={}",
                                    mainCtx, verticleCtx);
                            injectMainBeansIntoVerticleBeanContext(verticleCtx, mainCtx);
                            return verticleCtx;
                        })
                        .flatMap(verticleCtx -> {
                            var mainCtx = applicationBeanContext;
                            var vertx = mainCtx.getBean(Vertx.class);
                            log.debug("deploying micronaut verticle: producerName={}, verticleCtx={}",
                                    verticleProducer.getName(), verticleCtx);
                            verticleProducer.setVerticleBeanCtx(verticleCtx);
                            return vertx.rxDeployVerticle(verticleProducer, opts)
                                    .doOnSuccess(depId -> {
                                        log.debug("micronaut verticle deployed: depId={}, verticleCtx={}",
                                                depId, verticleCtx);
                                        verticlesBeanContexts.put(depId, verticleCtx);
                                    });
                        })
                );
    }

    public final Completable undeployVerticle(String deploymentId) {
        return Single
                .fromCallable(() -> applicationBeanContext.getBean(Vertx.class))
                .flatMapCompletable(vertx -> {
                    log.debug("undeploying micronaut verticle: depId={}", deploymentId);
                    return vertx.rxUndeploy(deploymentId)
                            .doOnComplete(() -> log.debug("micronaut verticle undeployed: depId={}", deploymentId));
                })
                .andThen(Completable.fromAction(() -> {
                    var verticleCtx = verticlesBeanContexts.remove(deploymentId);
                    log.debug("stopping micronaut verticle bean ctx: depId={}, verticleCtx={}",
                            deploymentId, verticleCtx);
                    // TODO: seems that before destroying per-verticle ctx, we need to remove all app-wide beans from
                    //  it to not destroy them as they are still used by other verticles
                    verticleCtx.stop();
                    log.debug("micronaut verticle bean ctx stopped: depId={}, verticleCtx={}",
                            deploymentId, verticleCtx);
                }));
    }

    public final AppState getAppState() {
        return appState;
    }

    /**
     * Initial verticle producers for this app that must be deployed during the startup.
     * Rest of the verticles ca be simply deployed/undeployed with {@link #deployVerticle(VerticleProducer)} and
     * {@link #undeployVerticle(String)} methods.
     *
     * <p>Designed for inheritance. Subclasses may override this method to provide list of the verticles to be deployed
     * on startup.
     *
     * @return list of verticle producers to be used on app startup, empty list by default
     */
    public List<VerticleProducer<?>> getVerticlesProducers() {
        return Collections.emptyList();
    }

    /**
     * Extracted {@link ApplicationContext#builder()} into a factory method for unit tests support.
     * Unit tests may return mock from this method.
     *
     * @return new application context
     */
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        return ApplicationContext.builder();
    }

    protected final ApplicationContext getMainApplicationContext() {
        return applicationBeanContext;
    }

    protected final ApplicationContext getVerticleApplicationContext(String verticleDeploymentId) {
        return verticlesBeanContexts.get(verticleDeploymentId);
    }

    private Single<ApplicationContext> startAppContext() {
        return Single
                .fromCallable(() -> {
                    log.debug("starting main bean context");
                    var appCtx = newApplicationContextBuilder()
                            .properties(Map.of(PROP_IS_APP_BEAN_CTX, true))
                            .start();
                    log.debug("main bean context started");
                    return appCtx;
                });
    }

    private void injectMainBeansIntoVerticleBeanContext(ApplicationContext verticleCtx, ApplicationContext mainCtx) {
        verticleCtx.registerSingleton(ApplicationContext.class, mainCtx, Qualifiers.byStereotype(AppBean.class));
        var vertx = mainCtx.getBean(Vertx.class);
        verticleCtx.registerSingleton(Vertx.class, vertx, Qualifiers.byStereotype(AppBean.class));
    }
}
