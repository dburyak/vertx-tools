package com.dburyak.vertx.core.validation;

import com.dburyak.vertx.core.config.DurationTypeConverter;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Inject;

import java.time.Duration;

public class MinDurationValidator implements ConstraintValidator<MinDuration, Duration> {
    private DurationTypeConverter durationTypeConverter;

    @Override
    public boolean isValid(Duration value, AnnotationValue<MinDuration> annotationMetadata,
            ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        var minDurationParam = annotationMetadata.get("value", String.class);
        if (minDurationParam.isEmpty()) {
            throw new IllegalArgumentException("@MinDuration must have \"value\" specified");
        }
        var minDurationStr = minDurationParam.get().strip().toLowerCase();
        var minDuration = durationTypeConverter.convert(minDurationStr, Duration.class).get();
        return value.compareTo(minDuration) >= 0;
    }

    @Inject
    public void setDurationTypeConverter(DurationTypeConverter durationTypeConverter) {
        this.durationTypeConverter = durationTypeConverter;
    }
}
