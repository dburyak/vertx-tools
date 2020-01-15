package com.archiuse.mindis

/**
 * Mindis checked exception.
 */
class MindisException extends Exception implements DescriptiveThrowable {
    MindisException() {
        super()
    }

    MindisException(String message) {
        super()
        this.message = message
    }

    MindisException(String message, Throwable cause) {
        super(null, cause)
        this.message = message
    }

    MindisException(Throwable cause) {
        super(cause)
    }

    protected MindisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(null, cause, enableSuppression, writableStackTrace)
        this.message = message
    }
}
