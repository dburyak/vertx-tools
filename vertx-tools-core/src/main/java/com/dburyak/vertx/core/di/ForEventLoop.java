package com.dburyak.vertx.core.di;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bean qualifier for beans specific to event loop context, i.e. beans which are designed to be used on the event-loop
 * and are non-blocking.
 * E.g. thread-pools or schedulers for event-loop only, non-blocking strategies designed for execution on the
 * event loop only, etc.
 */
@Qualifier
@Retention(RUNTIME)
@Documented
public @interface ForEventLoop {
}
