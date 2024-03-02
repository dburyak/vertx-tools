package com.dburyak.vertx.core.validation;

import com.dburyak.vertx.core.config.Memory;
import com.dburyak.vertx.core.config.MemoryTypeConverter;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

/**
 * Validator implementation for {@link MaxMem} annotation.
 */
@RequiredArgsConstructor
public class MaxMemValidator implements ConstraintValidator<MaxMem, Memory> {
    private final MemoryTypeConverter memoryTypeConverter;

    @Override
    public boolean isValid(@Nullable Memory value, @NonNull AnnotationValue<MaxMem> annotationMetadata,
            @NonNull ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        var maxMemParam = annotationMetadata.get("value", String.class);
        if (maxMemParam.isEmpty()) {
            throw new IllegalArgumentException("@MaxMem must have \"value\" specified");
        }
        var maxMemStr = maxMemParam.get().strip().toLowerCase();
        var maxMem = memoryTypeConverter.convert(maxMemStr, Memory.class).get();
        return value.compareTo(maxMem) <= 0;
    }
}
