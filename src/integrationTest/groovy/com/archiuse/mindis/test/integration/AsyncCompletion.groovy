package com.archiuse.mindis.test.integration

import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.ElementType.TYPE
import static java.lang.annotation.RetentionPolicy.RUNTIME

@Retention(RUNTIME)
@Target([TYPE, METHOD])
@interface AsyncCompletion {
    int numActions() default 1
}
