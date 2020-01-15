package com.archiuse.mindis.call

import groovy.transform.InheritConstructors

@InheritConstructors
class WrongServiceTypeException extends CallException {
    ServiceType expectedType
    ServiceType actualType
}
