package com.dburyak.vertx.core.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validation annotation for max duration.
 */
@Retention(RUNTIME)
@Constraint(validatedBy = MaxDurationValidator.class)
public @interface MaxDuration {

    /**
     * Maximal duration value.
     *
     * @return maximal duration value
     */
    String value();

    /**
     * Message to be used for validation error.
     *
     * @return message to be used for validation error
     */
    String message() default "duration must be less or equal to maximal: max={value}, provided={validatedValue}";
}
