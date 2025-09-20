package com.dburyak.vertx.core;

import io.reactivex.rxjava3.core.Completable;

/**
 * Object that can be initialized asynchronously.
 * Async initialization of beans implementing this interface happens AFTER the synchronous initialization
 * (constructors and {@link jakarta.annotation.PostConstruct} annotated methods) is completed, but BEFORE
 * the bean can be used by verticles (before calling verticle startup method).
 * <p>
 * There is no any order guarantee of async initialization between multiple beans. Beans dependencies are taken into
 * account only for synchronous initialization phase (constructors and {@link jakarta.annotation.PostConstruct}
 * methods), but not for async initialization. Async init will happen in parallel for all the beans implementing
 * this interface.
 * <p>
 * Beans implementing this interface will <b>NOT</b> be detected by DI container automatically when creating
 * <b>lazy</b> (default) beans.
 * Unfortunately, there is no such mechanism in Micronaut DI, as it's blocking by design.
 * Automatic handling of this interface is only supported for <b>eager</b> beans annotated with one of
 * {@link com.dburyak.vertx.core.di.AppStartup} or {@link com.dburyak.vertx.core.di.VerticleStartup} qualifiers.
 * It's possible to implement it for all beans in the future, but it will require substantial effort.
 * There are two places where lazy beans creation is triggered - before deploying verticles and when injecting
 * dependencies into each verticle. And the last one is tricky to handle properly for any scope other than
 * VerticleScope. In order to guarantee "only once" async initialization, we need to keep track of which bean instances
 * have already <em>requested</em> (not necessarily completed) async initialization, and request it only once per
 * scope. Also, the registry with this information must be thread-safe as multiple verticles will perform async init
 * concurrently during their startup. Will leave this for the future if there is a real need for it.
 */
public interface AsyncInitializable {

    /**
     * Perform async initialization.
     * For eager singleton beans, this method can be called on any thread, no assumptions should be made.
     * For verticle-scoped beans, this method is guaranteed to be called on the corresponding
     * verticle context/thread.
     */
    Completable initAsync();
}
