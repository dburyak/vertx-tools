package com.dburyak.vertx.core.validation;

import com.dburyak.vertx.core.config.DurationTypeConverter;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * Validator implementation for {@link MinDuration} annotation.
 */
@RequiredArgsConstructor
public class MinDurationValidator implements ConstraintValidator<MinDuration, Duration> {
    private final DurationTypeConverter durationTypeConverter;

    @Override
    public boolean isValid(Duration value, AnnotationValue<MinDuration> annotationMetadata,
            ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        var minDurationParam = annotationMetadata.get("value", String.class);
        if (minDurationParam.isEmpty()) {
            throw new IllegalArgumentException("@MinDuration must have \"value\" specified");
        }
        var minDurationStr = minDurationParam.get().strip().toLowerCase();
        var minDuration = durationTypeConverter.convert(minDurationStr, Duration.class).get();
        return value.compareTo(minDuration) >= 0;
    }
}
