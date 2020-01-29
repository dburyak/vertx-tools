package com.archiuse.mindis.test.integration

import io.reactivex.Completable

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

import static java.util.concurrent.TimeUnit.MILLISECONDS

class Async {
    private final int totalSteps
    private final AtomicInteger stepsLeft
    private final CompletableFuture<Void> completableFuture = new CompletableFuture<>()

    Async(int totalSteps = 1) {
        this.totalSteps = totalSteps
        this.stepsLeft = new AtomicInteger(totalSteps)
    }

    int getTotalSteps() {
        totalSteps
    }

    int getStepsDone() {
        totalSteps - stepsLeft.get()
    }

    int getStepsLeft() {
        stepsLeft.get()
    }

    void stepDone() {
        int oldValue, newValue
        do {
            oldValue = stepsLeft.get()
            if (oldValue == 0) {
                throw new IllegalStateException("stepDone invoked more than ${totalSteps} times")
            } else {
                newValue = oldValue - 1
            }
        } while (!stepsLeft.compareAndSet(oldValue, newValue))
        if (newValue <= 0) {
            completableFuture.complete(null)
        }
    }

    void complete() {
        int left = stepsLeft.getAndSet(0)
        if (left > 0) {
            completableFuture.complete(null)
        } else {
            throw new IllegalStateException(
                    "Async complete method has been called more than ${totalSteps} times, check your test.")
        }
    }

    void fail(Throwable error = null) {
        completableFuture.completeExceptionally(error ?: new Exception('Async execution flow failure'))
    }

    void fail(String errMsg) {
        completableFuture.completeExceptionally(new Exception("Async execution flow failure: ${errMsg}"))
    }

    boolean isCompleted() {
        return completableFuture.isDone()
    }

    boolean isSucceeded() {
        return isCompleted() && !isFailed()
    }

    boolean isFailed() {
        return completableFuture.isCompletedExceptionally()
    }

    void await(Duration timeout = null) {
        if (timeout) {
            try {
                completableFuture.get(timeout.toMillis(), MILLISECONDS)
            } catch (TimeoutException e) {
                throw new TimeoutException("Async operation took more than ${timeout}")
            }
        } else {
            completableFuture.get()
        }
    }

    Completable awaitCompletable(Duration timeout = null) {
        def result = Completable.create { e ->
            def isCancelled = new AtomicBoolean(false)
            completableFuture.whenComplete { ignr, err ->
                if (!isCancelled.get()) {
                    if (!err) {
                        e.onComplete()
                    } else {
                        e.onError(err)
                    }
                }
            }
            e.cancellable = { isCancelled.set(true) }
        }
        !timeout ? result : result.timeout(timeout.toMillis(), MILLISECONDS)
    }

    void doAssert(Closure<Void> assertions) {
        try {
            assertions()
        } catch (e) {
            fail e
        }
    }

    void doAssertAndMarkStepDone(Closure<Void> assertions) {
        try {
            assertions()
            stepDone()
        } catch (e) {
            fail e
        }
    }

    void doAssertAndComplete(Closure<Void> assertions) {
        try {
            assertions()
            complete()
        } catch (e) {
            fail e
        }
    }
}

