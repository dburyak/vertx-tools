package com.archiuse.mindis.config

import groovy.transform.InheritConstructors

/**
 * JsonArray/List/Collection config entry contains nested non-plain objects.
 * Lists used in config should be always flatten-able, for example should be convertable to comma separated list.
 */
@InheritConstructors
class ComplexListConfigException extends MalformedConfigException {

    /**
     * Collection where complex object was encountered. "list" name is used for Sets, Lists and arrays as it's
     * simpler and  shorter and in this context uniqueness or ordering doesn't make any difference.
     */
    Iterable list

    Object complexElement
    Integer complexElementIndex
}
