package com.archiuse.mindis

/**
 * Mindis system error.
 */
class MindisError extends Error implements DescriptiveThrowable {
    MindisError() {
        super()
    }

    MindisError(String message) {
        super()
        this.message = message
    }

    MindisError(String message, Throwable cause) {
        super(null, cause)
        this.message = message
    }

    MindisError(Throwable cause) {
        super(cause)
    }

    protected MindisError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(null, cause, enableSuppression, writableStackTrace)
        this.message = message
    }
}
