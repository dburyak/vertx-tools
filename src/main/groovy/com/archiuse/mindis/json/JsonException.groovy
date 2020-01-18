package com.archiuse.mindis.json

import com.archiuse.mindis.MindisException
import groovy.transform.InheritConstructors
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@InheritConstructors
class JsonException extends MindisException {
    String jsonStr
    JsonObject jsonObject
    JsonArray jsonArray
    Object jsonGeneric
}
