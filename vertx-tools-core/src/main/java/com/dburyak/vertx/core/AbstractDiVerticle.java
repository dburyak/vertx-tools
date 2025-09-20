package com.dburyak.vertx.core;

import com.dburyak.vertx.core.di.VerticleBeanBaseClass;
import com.dburyak.vertx.core.di.VerticleScopeImpl;
import com.dburyak.vertx.core.di.VerticleStartup;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.core.AbstractVerticle;
import lombok.Setter;

/**
 * Verticle base class with DI support.
 * <p>
 * This is a base building block for actor-based system that adds important features on top of Vertx
 * {@link io.vertx.core.AbstractVerticle}/{@link AbstractVerticle}. Implementations should be used through
 * {@link VertxDiApp} instead of default Vertx mechanisms otherwise DI won't work.
 * <p>
 * DI verticles have 2 major limitations compared to regular DI beans:
 * <ul>
 *     <li>must provide default constructor
 *     <li>cannot use constructor-based injection, only setter-based injection is allowed
 * </ul>
 * No extra measures are needed to make verticles thread-safe for non-final fields for injected beans.
 * Injection always happens on the same EventLoop thread that is assigned to verticle. So DI verticles remain
 * thread-safe with simple modifiable fields for injected beans (as long as injected beans themselves are thread safe
 * within their declared scope of course).
 * <p>
 * This base class has already all the necessary scope annotations in place. Only
 * {@link io.micronaut.context.annotation.Bean} annotations may be needed to explicitly mark it as a bean. This may fix
 * issues with lombok {@link Setter#onMethod} or {@link Setter#onParam} used in DiVerticles.
 * Subclasses should not put any scope annotations since instantiation and initialization of DI Verticles
 * happens not through regular DI mechanisms but through manual bean creation and injection.
 * Instead, use standard vertx verticle {@link io.vertx.core.DeploymentOptions} specified as part of
 * {@link VerticleDeploymentDescriptor} provided for {@link VertxDiApp} for controlling number of instances.
 * <p>
 * DiVerticle deployment happens in five phases:
 * <ul>
 *     <li>verticle instantiation with default constructor on the calling thread
 *     <li>deployment initial phase on the calling thread
 *     <li>dependency injection on the vertx event loop thread as part of Verticle.init(Vertx, Context) call
 *     <li>verticle initialization routine on the vertx event loop thread
 *     <li>verticle startup routine on the vertx event loop thread
 * </ul>
 * Thus, all implementations must provide default constructor for the first phase.
 * <p>
 * Dependency injection through constructor arguments is not possible due to vertx internal design - it creates
 * verticle instance on the calling thread, not on the event loop context thread.
 * Performing beans injection on vertx event loop thread allows to avoid visibility issues when using stateful
 * prototype-scoped, or event-loop-scoped, or verticle-scoped beans. This approach makes it possible to take advantage
 * of all the single-threaded threading model benefits without putting any effort in safe publishing of
 * stateful beans code. The tradeoff - unable to use constructor-based injection on verticles. Though constructor-based
 * injection can be safely used on any beans other than verticles.
 */
@VerticleBeanBaseClass
public abstract class AbstractDiVerticle extends AbstractVerticle {

    /**
     * DI application context. Is set by {@link VertxDiApp} during verticle deployment.
     */
    protected volatile ApplicationContext appCtx;

    @Override
    public final void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        // this call triggers DI on the vertx event loop thread assigned to this verticle
        appCtx.registerSingleton(this);
        doOnInit(vertx, context);
    }

    @Override
    public final Completable rxStart() {
        var thisVerticleClass = getClass();
        return Single.fromCallable(() -> appCtx.getBeanDefinitions(Object.class,
                        Qualifiers.byStereotype(VerticleStartup.class)))
                .flatMapCompletable(allVerticleStartupBeans -> {
                    var asyncVerticleStartupActions = allVerticleStartupBeans.stream()
                            .filter(beanDef -> {
                                var metadata = beanDef.getAnnotationMetadata();
                                var requiredVerticleType = metadata.classValue(VerticleStartup.class, "value")
                                        .orElse(Object.class);
                                return requiredVerticleType.isAssignableFrom(thisVerticleClass);
                            })
                            // this triggers synchronous initialization of the bean
                            .map(beanDef -> appCtx.getBean(beanDef.getBeanType()))
                            .filter(AsyncInitializable.class::isInstance)
                            .map(AsyncInitializable.class::cast)
                            .map(AsyncInitializable::initAsync)
                            .toList();
                    return Completable.merge(asyncVerticleStartupActions);
                })
                .andThen(startup());
    }

    @Override
    public final void start(Promise<Void> startFuture) {
        rxStart().subscribe(startFuture::complete, startFuture::fail);
    }

    @Override
    public final void start() throws Exception {
        // fixed as no-op here to avoid confusion in subclasses - allow only "startup" method to be overridden as the
        // only way to define verticle startup logic
    }

    @Override
    public final Completable rxStop() {
        return shutdown().andThen(Completable.defer(() -> {
            var verticleScopeImpl = appCtx.getBean(VerticleScopeImpl.class);
            return verticleScopeImpl.destroyScopeForThisVerticle();
        }));
    }

    @Override
    public final void stop(Promise<Void> stopFuture) throws Exception {
        rxStop().subscribe(stopFuture::complete, stopFuture::fail);
    }

    @Override
    public final void stop() throws Exception {
        // fixed as no-op here to avoid confusion in subclasses - allow only "shutdown" method to be overridden as
        // the only way to define verticle shutdown logic
    }

    /**
     * Subclasses can define verticle startup logic in this method. At the point of this method invocation, all the
     * dependencies are already injected and initialized, including {@link com.dburyak.vertx.core.di.VerticleStartup}
     * beans.
     */
    protected Completable startup() {
        // subclasses can define startup logic in this method
        return Completable.complete();
    }

    /**
     * Subclasses can define verticle shutdown logic in this method. Is invoked as the very first step of verticle
     * shutdown routine, if we only undeploy some verticle(s). And therefore as the very first step of the application
     * shutdown routine if we are shutting down the whole application.
     */
    protected Completable shutdown() {
        // subclasses can define shutdown logic in this method
        return Completable.complete();
    }

    /**
     * Subclasses can extend verticle initialization behavior with this method.
     *
     * @param vertx vertx
     * @param context vertx execution context
     */
    protected void doOnInit(Vertx vertx, Context context) {
        // subclasses can extend verticle initialization behavior with this method
    }

    // package private setter called by VertxDiApp, not supposed to be used for any other purposes
    void setVertx(io.vertx.rxjava3.core.Vertx vertx) {
        this.vertx = vertx;
    }

    // package private setter called by VertxDiApp, not supposed to be used for any other purposes
    void setAppCtx(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }
}
