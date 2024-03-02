package com.dburyak.vertx.core.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validation annotation for minimal memory.
 */
@Retention(RUNTIME)
@Constraint(validatedBy = MinMemValidator.class)
public @interface MinMem {

    /**
     * Minimal memory value.
     *
     * @return minimal memory value
     */
    String value();

    /**
     * Message to be used for validation error.
     *
     * @return message to be used for validation error
     */
    String message() default "memory must be greater or equal to minimal: min={value}, provided={validatedValue}";
}
