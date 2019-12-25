package com.archiuse.mindis.config

import groovy.transform.InheritConstructors

/**
 * Configuration of unexpected/unsupported format.
 */
@InheritConstructors
class MalformedConfigException extends ConfigException {
    String key
    Object value
}
