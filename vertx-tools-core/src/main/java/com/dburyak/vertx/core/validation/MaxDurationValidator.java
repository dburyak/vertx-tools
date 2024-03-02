package com.dburyak.vertx.core.validation;


import com.dburyak.vertx.core.config.DurationTypeConverter;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * Validator implementation for {@link MaxDuration} annotation.
 */
@RequiredArgsConstructor
public class MaxDurationValidator implements ConstraintValidator<MaxDuration, Duration> {
    private final DurationTypeConverter durationTypeConverter;

    @Override
    public boolean isValid(@Nullable Duration value, @NonNull AnnotationValue<MaxDuration> annotationMetadata,
            @NonNull ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        var maxDurationParam = annotationMetadata.get("value", String.class);
        if (maxDurationParam.isEmpty()) {
            throw new IllegalArgumentException("@MaxDuration must have \"value\" specified");
        }
        var maxDurationStr = maxDurationParam.get().strip().toLowerCase();
        var maxDuration = durationTypeConverter.convert(maxDurationStr, Duration.class).get();
        return value.compareTo(maxDuration) <= 0;
    }
}
