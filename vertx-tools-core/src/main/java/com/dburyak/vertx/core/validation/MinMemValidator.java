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
 * Validation annotation for minimal memory.
 */
@RequiredArgsConstructor
public class MinMemValidator implements ConstraintValidator<MinMem, Memory> {
    private final MemoryTypeConverter memoryTypeConverter;

    @Override
    public boolean isValid(@Nullable Memory mem, @NonNull AnnotationValue<MinMem> annotationMetadata,
            @NonNull ConstraintValidatorContext context) {
        if (mem == null) {
            return true;
        }
        var minMemParam = annotationMetadata.get("value", String.class);
        if (minMemParam.isEmpty()) {
            throw new IllegalArgumentException("@MinMem must have \"value\" specified");
        }
        var minMemStr = minMemParam.get().strip().toLowerCase();
        var minMem = memoryTypeConverter.convert(minMemStr, Memory.class).get();
        return mem.compareTo(minMem) >= 0;
    }
}
