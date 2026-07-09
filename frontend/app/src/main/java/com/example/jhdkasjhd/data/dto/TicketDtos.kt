package com.example.jhdkasjhd.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TicketCreateRequest(
    @Json(name = "event_id") val eventId: String,
    @Json(name = "form_response") val formResponse: Map<String, Any?> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class TicketResponse(
    val id: String,
    @Json(name = "event_id") val eventId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "qr_signature") val qrSignature: String,
    val status: String,
    @Json(name = "form_response") val formResponse: Map<String, Any?> = emptyMap(),
    @Json(name = "registered_at") val registeredAt: String
)

@JsonClass(generateAdapter = true)
data class CheckinRequest(
    @Json(name = "ticket_id") val ticketId: String,
    @Json(name = "event_id") val eventId: String,
    @Json(name = "qr_signature") val qrSignature: String
)

@JsonClass(generateAdapter = true)
data class CheckinResponse(
    val id: String,
    @Json(name = "ticket_id") val ticketId: String,
    @Json(name = "validated_by") val validatedBy: String?,
    @Json(name = "checkin_time") val checkinTime: String
)

@JsonClass(generateAdapter = true)
data class AnalyticsResponse(
    @Json(name = "event_id") val eventId: String,
    val capacity: Int,
    @Json(name = "total_registered") val totalRegistered: Int,
    @Json(name = "total_checked_in") val totalCheckedIn: Int,
    @Json(name = "occupancy_rate_percent") val occupancyRatePercent: Double,
    @Json(name = "attendance_rate_percent") val attendanceRatePercent: Double,
    val status: String
)
