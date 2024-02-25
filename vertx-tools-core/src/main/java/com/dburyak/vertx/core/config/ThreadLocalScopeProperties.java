package com.dburyak.vertx.core.config;

import com.dburyak.vertx.core.validation.MinDuration;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Duration;

/**
 * Configuration of {@link com.dburyak.vertx.core.di.ThreadLocalScope} bean scope.
 */
@ConfigurationProperties("vertx.di.scope.thread-local")
@Getter
public class ThreadLocalScopeProperties {

    /**
     * Cleanup checker period.
     * <p>
     * Checker runs periodically to find dead threads associated with bean contexts and destroy such dangling contexts
     * and beans.
     */
    @MinDuration("1s")
    @NotNull
    private final Duration cleanupCheckerPeriod;

    @ConfigurationInject
    public ThreadLocalScopeProperties(
            @Bindable(defaultValue = "5s") @NotNull @MinDuration("1s") Duration cleanupCheckerPeriod) {
        this.cleanupCheckerPeriod = cleanupCheckerPeriod;
    }
}
