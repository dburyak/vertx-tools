package com.archiuse.mindis.test.integration

import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation

class IntegrationAppStartingInterceptor extends AbstractMethodInterceptor {
    static volatile boolean isShutdownRegistered = false

    @Override
    void interceptSharedInitializerMethod(IMethodInvocation invocation) throws Throwable {
        VertxIntegrationApp.startIfNotRunning().blockingAwait()
        if (!isShutdownRegistered) {
            addShutdownHook {
                VertxIntegrationApp.stopIfRunning().blockingAwait()
            }
            isShutdownRegistered = true
        }
        invocation.proceed()
    }
}
