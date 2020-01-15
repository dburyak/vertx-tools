package com.archiuse.mindis.call

import groovy.transform.InheritConstructors

/**
 * Indicates that actor service is not registered via service discovery.
 */
@InheritConstructors
class ServiceNotFoundException extends CallException {
}
