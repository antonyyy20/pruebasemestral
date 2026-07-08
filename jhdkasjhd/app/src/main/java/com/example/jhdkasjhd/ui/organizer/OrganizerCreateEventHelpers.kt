package com.example.jhdkasjhd.ui.organizer

import org.json.JSONArray
import org.json.JSONObject

internal fun parseCustomFormSchema(raw: String): Result<Map<String, Any?>> = runCatching {
    val trimmed = raw.trim()
    require(trimmed.isNotEmpty()) { "El esquema del formulario es obligatorio" }
    val json = JSONObject(trimmed)
    require(json.length() > 0) { "El esquema del formulario no puede estar vacío" }
    jsonObjectToMap(json)
}

private fun jsonObjectToMap(json: JSONObject): Map<String, Any?> {
    val map = linkedMapOf<String, Any?>()
    val keys = json.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = jsonValueToKotlin(json.get(key))
    }
    return map
}

private fun jsonValueToKotlin(value: Any?): Any? = when (value) {
    JSONObject.NULL -> null
    is JSONObject -> jsonObjectToMap(value)
    is JSONArray -> (0 until value.length()).map { jsonValueToKotlin(value.get(it)) }
    else -> value
}

internal fun isValidHttpUrl(url: String): Boolean {
    val trimmed = url.trim()
    return trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
}
