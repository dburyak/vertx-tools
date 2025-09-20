package com.dburyak.vertx.core;

import io.reactivex.rxjava3.core.Completable;

/**
 * Object that can be closed asynchronously.
 * Async close happens before the synchronous destruction ({@link jakarta.annotation.PreDestroy} annotated methods) is
 * called.
 * <p>
 * Unlike {@link AsyncInitializable}, beans implementing this interface <b>WILL ALWAYS</b> be detected and handled
 * automatically by shutdown routine. Verticle-scoped beans will be closed when the verticle is undeployed, and all the
 * other beans will be closed on application shutdown. Async close will happen in parallel for all the beans
 * implementing this interface.
 * <p>
 * There is no any order guarantee of async closing between multiple beans. Beans dependencies are taken into
 * account only for synchronous destruction phase ({@link jakarta.annotation.PreDestroy} methods), but not for async
 * closing.
 */
public interface AsyncCloseable {

    /**
     * Asynchronously close the instance.
     * For verticle-scoped beans, this method is guaranteed to be called on the corresponding
     * verticle context/thread.
     * For any other scopes, this method can be called on any thread, no assumptions should be made. If the cleanup
     * operation requires to be performed on a specific thread or context, then the implementation
     * should capture the Vertx context upon creation and use it here during disposal.
     */
    Completable closeAsync();
}
