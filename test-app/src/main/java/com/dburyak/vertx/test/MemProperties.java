package com.dburyak.vertx.test;

import com.dburyak.vertx.core.config.Memory;
import com.dburyak.vertx.core.validation.MaxDuration;
import com.dburyak.vertx.core.validation.MaxMem;
import com.dburyak.vertx.core.validation.MinDuration;
import com.dburyak.vertx.core.validation.MinMem;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.bind.annotation.Bindable;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

@Context
@ConfigurationProperties("memory")
public class MemProperties {

    //    @Getter
    private final Memory value;

    //    @Getter
    private final Duration duration;

    @ConfigurationInject
    public MemProperties(
            @Bindable(defaultValue = "5mb") @MinMem("1mb") @MaxMem("10mb") @NotNull Memory value,
            @Bindable(defaultValue = "13s") @MinDuration("1s") @MaxDuration("10s") @NotNull Duration duration) {
        this.value = value;
        this.duration = duration;
    }

    public Memory getValue() {
        return value;
    }

    public Duration getDuration() {
        return duration;
    }
}
