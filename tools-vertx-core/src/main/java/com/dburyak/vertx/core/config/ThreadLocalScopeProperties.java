package com.dburyak.vertx.core.config;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import lombok.Data;

import java.time.Duration;

/**
 * Configuration of {@link com.dburyak.vertx.core.di.ThreadLocalScope} bean scope.
 */
@ConfigurationProperties("vertx.di.scope.thread-local")
@Context
@Data
public class ThreadLocalScopeProperties {

    /**
     * Cleanup checker period.
     * <p>
     * Checker runs periodically to find dead threads associated with bean contexts and destroy such dangling contexts
     * and beans.
     */
    @MinDuration("1s")
    private Duration cleanupCheckerPeriod = Duration.ofSeconds(5);
}
