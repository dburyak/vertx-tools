package com.archiuse.mindis.test.integration


import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation

import java.lang.reflect.Parameter

import static com.archiuse.mindis.test.integration.TestExecutionThread.SPOCK_JUNIT_THREAD
import static com.archiuse.mindis.test.integration.TestExecutionThread.VERTX_EL
import static com.archiuse.mindis.test.integration.TestExecutionThread.VERTX_WORKER

class VertxThreadingInterceptor extends AbstractMethodInterceptor {
    int numAsyncActions = 1
    TestExecutionThread testExecutionThread = VERTX_EL

    @Override
    void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {
        def mainAsync = new Async()
        def paramAsync = injectAsyncParam(invocation)

        // invoke feature method on specified thread
        invokeFeatureMethod(invocation, mainAsync)

        // wait for feature method completion on "Test Worker" thread
        try {
            paramAsync?.await()
            mainAsync.await()
        } catch (InterruptedException e) {
            // clear "interrupted" flag so no further code gets failing with "InterruptedException"
            Thread.interrupted()
            throw e
        }
    }

    private void invokeFeatureMethod(IMethodInvocation invocation, Async mainAsync) {
        switch (testExecutionThread) {
            case VERTX_EL:
                invokeFeatureMethodOnVertxElThread invocation, mainAsync
                break
            case VERTX_WORKER:
                invokeFeatureMethodOnVertxWorkerThread invocation, mainAsync
                break
            case SPOCK_JUNIT_THREAD:
                invokeFeatureMethodOnSpockJunitThread invocation, mainAsync
                break
            default:
                throw new IllegalArgumentException("testExecutionThread=${testExecutionThread}")
                break
        }
    }

    private void invokeFeatureMethodOnVertxElThread(IMethodInvocation invocation, Async mainAsync) {
        def spec = invocation.sharedInstance as VertxIntegrationSpec
        def vertxCtx = spec.integrationTestVerticle.vertxContext
        vertxCtx.runOnContext {
            invocation.proceed()
            mainAsync.complete()
        }
    }

    private void invokeFeatureMethodOnVertxWorkerThread(IMethodInvocation invocation, Async mainAsync) {
        def spec = invocation.sharedInstance as VertxIntegrationSpec
        def vertxCtx = spec.integrationTestVerticle.vertxContext
        vertxCtx
                .rxExecuteBlocking({ f ->
                    invocation.proceed()
                    f.complete()
                }, false)
                .subscribe({
                    // ignore value, should never be called anyway
                }, { err ->
                    mainAsync.fail(err)
                }, {
                    mainAsync.complete()
                })
    }

    private void invokeFeatureMethodOnSpockJunitThread(IMethodInvocation invocation, Async mainAsync) {
        invocation.proceed()
        mainAsync.complete()
    }

    private Async injectAsyncParam(IMethodInvocation invocation) {
        def async = null
        def (declaredAsyncParam, asyncParamPos) = invocation.method.reflection.parameters
                ?.toList()?.withIndex()
                ?.find { Parameter param, pos -> Async.isAssignableFrom(param.type) }
                ?: []
        if (declaredAsyncParam) {
            async = new Async(numAsyncActions)
            def newArgs = invocation.arguments.toList()
            newArgs[asyncParamPos] = async
            invocation.arguments = newArgs.toArray()
        }
        return async
    }
}
