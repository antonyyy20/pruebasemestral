package com.example.jhdkasjhd.ui.organizer

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal data class CreateEventValidation(
    val isValid: Boolean,
    val missingLabels: List<String>,
    val parsedCapacity: Int?,
    val parsedSchema: Result<Map<String, Any?>>,
    val hasValidBanner: Boolean,
    val hasValidRange: Boolean
)

internal fun validateCreateEventForm(
    eventTitle: String,
    description: String,
    category: String,
    location: String,
    capacity: String,
    bannerUrl: String,
    customFormSchema: String,
    startDate: LocalDate?,
    startTime: LocalTime?,
    endDate: LocalDate?,
    endTime: LocalTime?
): CreateEventValidation {
    val parsedCapacity = capacity.trim().toIntOrNull()
    val parsedSchema = parseCustomFormSchema(customFormSchema)
    val hasValidBanner = bannerUrl.isBlank() || isValidHttpUrl(bannerUrl)
    val hasValidDates = startDate != null && startTime != null && endDate != null && endTime != null
    val hasValidRange = if (hasValidDates) {
        val start = ZonedDateTime.of(startDate!!, startTime!!, ZoneId.systemDefault())
        val end = ZonedDateTime.of(endDate!!, endTime!!, ZoneId.systemDefault())
        !end.isBefore(start)
    } else {
        false
    }

    val missing = buildList {
        if (eventTitle.isBlank()) add("título")
        if (startDate == null) add("fecha de inicio")
        if (startTime == null) add("hora de inicio")
        if (endDate == null) add("fecha de fin")
        if (endTime == null) add("hora de fin")
        if (category.isBlank()) add("categoría")
        if (location.isBlank()) add("ubicación")
        if (description.isBlank()) add("descripción")
        if (parsedCapacity == null || parsedCapacity <= 0) add("capacidad")
        if (!hasValidBanner) add("URL del banner válida")
        if (parsedSchema.isFailure) add("JSON del formulario válido")
        if (hasValidDates && !hasValidRange) add("fecha/hora de fin posterior al inicio")
    }

    val isValid = missing.isEmpty()

    return CreateEventValidation(
        isValid = isValid,
        missingLabels = missing,
        parsedCapacity = parsedCapacity,
        parsedSchema = parsedSchema,
        hasValidBanner = hasValidBanner,
        hasValidRange = hasValidRange
    )
}

internal fun defaultCreateEventStartTime(): LocalTime = LocalTime.of(18, 0)

internal fun defaultCreateEventEndTime(startTime: LocalTime): LocalTime {
    val adjusted = startTime.plusHours(2)
    return if (adjusted.toSecondOfDay() < startTime.toSecondOfDay()) {
        LocalTime.of(23, 59)
    } else {
        adjusted
    }
}

internal data class StartDateDefaults(
    val startDate: LocalDate,
    val startTime: LocalTime,
    val endDate: LocalDate,
    val endTime: LocalTime
)

internal fun applyStartDateDefaults(
    selected: LocalDate,
    currentStartTime: LocalTime?,
    currentEndDate: LocalDate?,
    currentEndTime: LocalTime?
): StartDateDefaults {
    val startTime = currentStartTime ?: defaultCreateEventStartTime()
    val endDate = when {
        currentEndDate == null -> selected
        currentEndDate.isBefore(selected) -> selected
        else -> currentEndDate
    }
    val endTime = currentEndTime ?: defaultCreateEventEndTime(startTime)
    return StartDateDefaults(
        startDate = selected,
        startTime = startTime,
        endDate = endDate,
        endTime = endTime
    )
}

internal fun parseCustomFormSchema(raw: String): Result<Map<String, Any?>> = runCatching {
    val trimmed = raw.trim()
    if (trimmed.isEmpty() || trimmed == "{}") {
        return@runCatching emptyMap()
    }
    val json = JSONObject(trimmed)
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
