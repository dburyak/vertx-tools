package com.archiuse.mindis.call

import com.archiuse.mindis.MindisException
import groovy.transform.InheritConstructors

/**
 * Generic actor call exception.
 */
@InheritConstructors
class CallException extends MindisException {
    String receiver
    String action
}
