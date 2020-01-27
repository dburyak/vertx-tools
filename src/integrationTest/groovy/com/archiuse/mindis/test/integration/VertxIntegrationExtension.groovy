package com.archiuse.mindis.test.integration


import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.SpecInfo

import static com.archiuse.mindis.test.integration.TestExecutionThread.VERTX_EL_THREAD

class VertxIntegrationExtension extends AbstractAnnotationDrivenExtension<VertxIntegrationTest> {

    @Override
    void visitSpecAnnotation(VertxIntegrationTest annotation, SpecInfo spec) {
    }

    @Override
    void visitSpec(SpecInfo spec) {
        spec.addSharedInitializerInterceptor(new IntegrationAppStartingInterceptor())

        def specRunOn = spec.getAnnotation(RunOn)?.value() ?: VERTX_EL_THREAD
        def specAsyncActions = spec.getAnnotation(AsyncCompletion)?.numActions() ?: 1
        spec.allFeatures*.featureMethod.each {
            def runOn = it.getAnnotation(RunOn)?.value() ?: specRunOn
            def asyncActions = it.getAnnotation(AsyncCompletion)?.numActions() ?: specAsyncActions
            it.addInterceptor new VertxThreadingInterceptor(testExecutionThread: runOn, numAsyncActions: asyncActions)
        }
    }
}
