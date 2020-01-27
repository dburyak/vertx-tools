package com.archiuse.mindis.test.integration

import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.TYPE
import static java.lang.annotation.RetentionPolicy.RUNTIME

@Retention(RUNTIME)
@Target([TYPE])
@ExtensionAnnotation(VertxIntegrationExtension)
@interface VertxIntegrationTest {
}
