package com.archiuse.mindis.call

import groovy.transform.InheritConstructors

@InheritConstructors
class MalformedEbAddressNameException extends CallSetupException {
    String addr
}
