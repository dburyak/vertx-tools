package com.archiuse.mindis.call

import com.archiuse.mindis.SetupException
import groovy.transform.InheritConstructors

@InheritConstructors
class CallSetupException extends SetupException {
    String receiver
    String action
    ServiceType serviceType
}
