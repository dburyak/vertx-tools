package com.archiuse.mindis

/**
 * Mindis unchecked runtime exception.
 */
class MindisRuntimeException extends RuntimeException implements DescriptiveThrowable {
    MindisRuntimeException() {
        super()
    }

    MindisRuntimeException(String message) {
        super()
        this.message = message
    }

    MindisRuntimeException(String message, Throwable cause) {
        super(null, cause)
        this.message = message
    }

    MindisRuntimeException(Throwable cause) {
        super(cause)
    }

    protected MindisRuntimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(null, cause, enableSuppression, writableStackTrace)
        this.message = message
    }
}
