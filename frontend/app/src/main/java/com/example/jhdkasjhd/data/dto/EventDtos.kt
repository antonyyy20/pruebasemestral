package com.example.jhdkasjhd.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventResponse(
    val id: String,
    @Json(name = "organizer_id") val organizerId: String,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    @Json(name = "date_start") val dateStart: String,
    @Json(name = "date_end") val dateEnd: String,
    val capacity: Int,
    @Json(name = "banner_url") val bannerUrl: String? = null,
    val status: String,
    @Json(name = "custom_form_schema") val customFormSchema: Map<String, Any?> = emptyMap(),
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class EventCreateRequest(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    @Json(name = "date_start") val dateStart: String,
    @Json(name = "date_end") val dateEnd: String,
    val capacity: Int,
    @Json(name = "banner_url") val bannerUrl: String? = null,
    @Json(name = "custom_form_schema") val customFormSchema: Map<String, Any?> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class EventUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val location: String? = null,
    @Json(name = "date_start") val dateStart: String? = null,
    @Json(name = "date_end") val dateEnd: String? = null,
    val capacity: Int? = null,
    @Json(name = "banner_url") val bannerUrl: String? = null,
    val status: String? = null,
    @Json(name = "custom_form_schema") val customFormSchema: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class StaffCreateRequest(
    val email: String,
    val password: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class StaffMemberResponse(
    val id: String,
    @Json(name = "event_id") val eventId: String,
    @Json(name = "user_id") val userId: String,
    val name: String,
    val role: String,
    @Json(name = "assigned_at") val assignedAt: String
)
