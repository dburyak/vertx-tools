package com.dburyak.vertx.core.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validation annotation for minimal duration.
 */
@Retention(RUNTIME)
@Constraint(validatedBy = MinDurationValidator.class)
public @interface MinDuration {

    /**
     * Minimal duration value.
     *
     * @return minimal duration value
     */
    String value();

    /**
     * Message to be used for validation error.
     *
     * @return message to be used for validation error
     */
    String message() default "duration must be greater or equal to minimal: min={value}, provided={validatedValue}";
}
