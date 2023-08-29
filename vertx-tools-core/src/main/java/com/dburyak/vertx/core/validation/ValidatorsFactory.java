package com.dburyak.vertx.core.validation;

import com.dburyak.vertx.core.config.DurationTypeConverter;
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
}
