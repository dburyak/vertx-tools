package com.dburyak.vertx.core.validation;

import javax.validation.Constraint;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = MinDurationValidator.class)
public @interface MinDuration {

    /**
     * Minimal duration value.
     */
    String value();

    String message() default "duration must be greater or equal to minimal: min={value}, provided={validatedValue}";
}
