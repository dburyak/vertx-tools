package com.archiuse.mindis

import groovy.transform.SelfType

@SelfType(Throwable)
trait DescriptiveThrowable {
    String message

    String getMessage() {
        this.@message ?:
                metaPropertyValues
                        .findAll { it.name !in ['message', 'class', 'suppressed', 'localizedMessage', 'stackTrace'] }
                        .collect { "${it.name}=${it.value}" }
                        .join(', ')
    }
}
