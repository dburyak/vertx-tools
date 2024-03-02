package com.dburyak.vertx.core.validation;

import com.dburyak.vertx.core.config.DurationTypeConverter;
import com.dburyak.vertx.core.config.Memory;
import com.dburyak.vertx.core.config.MemoryTypeConverter;
import io.micronaut.context.annotation.Factory;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import jakarta.inject.Singleton;

import java.time.Duration;

/**
 * Factory for validation related beans.
 */
@Factory
public class ValidatorsFactory {

    /**
     * Validator for {@link MinDuration} annotation.
     *
     * @param durationTypeConverter duration type converter
     *
     * @return validator for {@link MinDuration} annotation
     */
    @Singleton
    public ConstraintValidator<MinDuration, Duration> minDurationConstraintValidator(
            DurationTypeConverter durationTypeConverter) {
        return new MinDurationValidator(durationTypeConverter);
    }

    /**
     * Validator for {@link MaxDuration} annotation.
     *
     * @param durationTypeConverter duration type converter
     *
     * @return validator for {@link MaxDuration} annotation
     */
    @Singleton
    public ConstraintValidator<MaxDuration, Duration> maxDurationConstraintValidator(
            DurationTypeConverter durationTypeConverter) {
        return new MaxDurationValidator(durationTypeConverter);
    }

    /**
     * Validator for {@link MinMem} annotation.
     *
     * @param memoryTypeConverter memory type converter
     *
     * @return validator for {@link MinMem} annotation
     */
    @Singleton
    public ConstraintValidator<MinMem, Memory> minMemConstraintValidator(MemoryTypeConverter memoryTypeConverter) {
        return new MinMemValidator(memoryTypeConverter);
    }

    /**
     * Validator for {@link MaxMem} annotation.
     *
     * @param memoryTypeConverter memory type converter
     *
     * @return validator for {@link MaxMem} annotation
     */
    @Singleton
    public ConstraintValidator<MaxMem, Memory> maxMemConstraintValidator(MemoryTypeConverter memoryTypeConverter) {
        return new MaxMemValidator(memoryTypeConverter);
    }
}
