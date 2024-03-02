package com.dburyak.vertx.core.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validation annotation for maximal memory.
 */
@Retention(RUNTIME)
@Constraint(validatedBy = MaxMemValidator.class)
public @interface MaxMem {

    /**
     * Maximal memory value.
     *
     * @return maximal memory value
     */
    String value();

    /**
     * Message to be used for validation error.
     *
     * @return message to be used for validation error
     */
    String message() default "memory must be less or equal to maximal: max={value}, provided={validatedValue}";
}
