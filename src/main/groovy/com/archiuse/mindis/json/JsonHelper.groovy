package com.archiuse.mindis.json

import com.archiuse.mindis.util.TemporalCategory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

import javax.inject.Singleton
import java.time.Duration
import java.time.temporal.Temporal

@Singleton
class JsonHelper {
    private static final String SPECIAL_TYPE_KEY = 'special_type'
    private static final String SPECIAL_VALUE_KEY = 'special_value'
    private static final String SPECIAL_TYPE_ENUM = 'enum'
    private static final String SPECIAL_ENUM_CLASS_KEY = 'special_enum_class'
    private static final String SPECIAL_TYPE_BYTE_ARRAY = 'byte_array'

    /**
     * Convert json to nested map. Works similar to groovy's JsonSlurper but also decodes:
     * <ul>
     *   <li>strings with floating point numbers to BigDecimal-s</li>
     *   <li>byte arrays (binary) special objects to java byte arrays</li>
     *   <li>strings with time/date/timestamps to corresponding java.util.time objects</li>
     *   <li>enum special objects to corresponding enum value instance</li>
     * </ul>
     * @param json vertx json object
     * @param decodeSpecial whether special data structures should be interpreted (default: true)
     * @return map
     */
    Map<String, Object> toMap(JsonObject json, boolean decodeSpecial = true) {
        fromJsonRecursively json, decodeSpecial
    }

    /**
     * Convert json array to list. Works similar to groovy's JsonSlurper but also decodes:
     * <ul>
     *   <li>strings with floating point numbers to BigDecimal-s</li>
     *   <li>byte arrays (binary) special objects to java byte arrays</li>
     *   <li>strings with time/date/timestamps to corresponding java.util.time objects</li>
     *   <li>enum special objects to corresponding enum value instance</li>
     * </ul>
     * @param json vertx json object
     * @param decodeSpecial whether special data structures should be interpreted (default: true)
     * @return list
     */
    List toList(JsonArray jsonArray, boolean decodeSpecial = true) {
        fromJsonRecursively jsonArray, decodeSpecial
    }

    /**
     * Convert groovy map to vertx json object. Special values are converted as follows:
     * <ul>
     *   <li>BigDecimal, Float, Double are represented as BigDecimal strings</li>
     *   <li>byte arrays are converted to special json objects with type specified</li>
     *   <li>java.util.time objects are converted to special json objects with type specified</li>
     *   <li>enums are converted to special json objects with type specified</li>
     * </ul>
     * @param map map to convert to json
     * @param encodeSpecial whether special data types should be converted (default: true)
     * @return vertx json object
     */
    JsonObject toJson(Map<String, Object> map, boolean encodeSpecial = true) {
        toJsonRecursively map, encodeSpecial
    }

    /**
     * Convert iterable (collection, list, array, etc.) to vertx json array. Special values are converted as follows:
     * <ul>
     *   <li>BigDecimal, Float, Double are represented as BigDecimal strings</li>
     *   <li>byte arrays are converted to special json objects with type specified</li>
     *   <li>java.util.time objects are converted to special json objects with type specified</li>
     *   <li>enums are converted to special json objects with type specified</li>
     * </ul>
     * @param iterable iterable to convert to json array
     * @param encodeSpecial whether special data types should be converted (default: true)
     * @return vertx json array
     */
    JsonArray toJson(Iterable iterable, boolean encodeSpecial = true) {
        toJsonRecursively iterable, encodeSpecial
    }

    JsonObject toJson(Object obj, boolean encodeSpecial = true) {
        JsonObject.mapFrom(obj)
    }

    def <T> T toObject(JsonObject json, Class<T> type) {
        json.mapTo(type)
    }


    // **************** OBJECT ===> JSON *********************
    private JsonObject toJsonRecursively(Map<String, Object> map, boolean encodeSpecial) {
        map.inject(new JsonObject()) { json, k, v ->
            json.put(k, v == null ? v : toJsonRecursively(v, encodeSpecial))
        }
    }

    private JsonArray toJsonRecursively(Iterable iterable, boolean encodeSpecial) {
        iterable.inject(new JsonArray()) { JsonArray jsonArr, val ->
            jsonArr.add(val == null ? val : toJsonRecursively(val, encodeSpecial))
        }
    }

    private def toJsonRecursively(Enum anEnum, boolean encodeSpecial) {
        if (!encodeSpecial) {
            anEnum.name()
        } else {
            new JsonObject([
                    (SPECIAL_TYPE_KEY)      : SPECIAL_TYPE_ENUM,
                    (SPECIAL_ENUM_CLASS_KEY): anEnum.getClass(),
                    (SPECIAL_VALUE_KEY)     : anEnum.name()
            ])
        }
    }

    private def toJsonRecursively(Number val, boolean encodeSpecial) {
        val
    }

    private def toJsonRecursively(Long val, boolean encodeSpecial) {
        val
    }

    private def toJsonRecursively(BigDecimal val, boolean encodeSpecial) {
        val as String
    }

    private def toJsonRecursively(BigInteger val, boolean encodeSpecial) {
        val as String
    }

    private def toJsonRecursively(Double val, boolean encodeSpecial) {
        encodeSpecial ? val as String : val
    }

    private def toJsonRecursively(Float val, boolean encodeSpecial) {
        encodeSpecial ? val as String : val
    }

    private def toJsonRecursively(byte[] bin, boolean encodeSpecial) {
        if (!encodeSpecial) {
            bin
        } else {
            new JsonObject([
                    (SPECIAL_TYPE_KEY) : SPECIAL_TYPE_BYTE_ARRAY,
                    (SPECIAL_VALUE_KEY): bin
            ])
        }
    }

    private def toJsonRecursively(Temporal val, boolean encodeSpecial) {
        val as String
    }

    private def toJsonRecursively(Duration val, boolean encodeSpecial) {
        val as String
    }

    private Object toJsonRecursively(Object obj, boolean encodeSpecial) {
        obj
    }


    // ************** JSON ===> OBJECT ******************
    private def fromJsonRecursively(JsonObject json, boolean decodeSpecial) {
        def isSpecialObject = decodeSpecial && json.containsKey(SPECIAL_TYPE_KEY) && json.containsKey(SPECIAL_VALUE_KEY)
        if (isSpecialObject) {
            fromJsonSpecial json
        } else {
            json.inject([:]) { map, e ->
                map[e.key] = (e.value == null) ? e.value : fromJsonRecursively(e.value, decodeSpecial)
                map
            }
        }
    }

    private def fromJsonSpecial(JsonObject jsonSpecial) {
        switch (jsonSpecial.getString(SPECIAL_TYPE_KEY)) {
            case SPECIAL_TYPE_ENUM:
                fromJsonSpecialEnum(jsonSpecial)
                break
            case SPECIAL_TYPE_BYTE_ARRAY:
                fromJsonSpecialByteArray(jsonSpecial)
                break
            default:
                throw new JsonException("unknown special type : ${jsonSpecial.getString(SPECIAL_TYPE_KEY)}")
        }
    }

    private def fromJsonSpecialByteArray(JsonObject jsonByteArray) {
        jsonByteArray.getBinary(SPECIAL_VALUE_KEY)
    }

    private def fromJsonSpecialEnum(JsonObject jsonEnum) {
        if (!jsonEnum.containsKey(SPECIAL_ENUM_CLASS_KEY)) {
            throw new JsonException()
        }
        Enum.valueOf(jsonEnum.getString(SPECIAL_ENUM_CLASS_KEY) as Class, jsonEnum.getString(SPECIAL_VALUE_KEY))
    }

    private List fromJsonRecursively(JsonArray jsonArray, boolean decodeSpecial) {
        jsonArray.inject([]) { list, val ->
            list << ((val == null) ? val : fromJsonRecursively(val, decodeSpecial))
        } as List
    }

    private def fromJsonRecursively(String str, boolean decodeSpecial) {
        if (str.isBigInteger()) {
            str.toBigInteger()
        } else if (str.isBigDecimal()) {
            str.toBigDecimal()
        } else {
            use(TemporalCategory) {
                str.toTemporal() ?: str
            }
        }
    }

    private def fromJsonRecursively(def obj, boolean decodeSpecial) {
        obj
    }
}
