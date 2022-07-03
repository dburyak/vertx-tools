package com.dburyak.vertx.core.di;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bean qualifier for beans specific to worker threads context, i.e. beans which are designed to be executed on worker
 * threads and are not suitable for execution on the event loop.
 * E.g. strategies implementations with blocking calls.
 */
@Qualifier
@Retention(RUNTIME)
@Documented
public @interface ForWorker {
}
