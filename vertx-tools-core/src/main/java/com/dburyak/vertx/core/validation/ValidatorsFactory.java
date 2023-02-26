package com.dburyak.vertx.core.validation;

import com.dburyak.vertx.core.config.DurationTypeConverter;
import io.micronaut.context.annotation.Factory;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import jakarta.inject.Singleton;

import java.time.Duration;

@Factory
public class ValidatorsFactory {

    @Singleton
    public ConstraintValidator<MinDuration, Duration> minDurationConstraintValidator(
            DurationTypeConverter durationTypeConverter) {
        var validator =  new MinDurationValidator();
        validator.setDurationTypeConverter(durationTypeConverter);
        return validator;
    }
}
