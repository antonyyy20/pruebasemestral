package com.example.jhdkasjhd.data.repository

import com.example.jhdkasjhd.core.network.mapNetworkError
import com.example.jhdkasjhd.core.network.QuickvntApi
import com.example.jhdkasjhd.data.dto.EventCreateRequest
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.EventUpdateRequest
import com.example.jhdkasjhd.data.dto.StaffAssignmentRequest
import com.example.jhdkasjhd.data.dto.StaffAssignmentResponse

class EventRepository(
    private val api: QuickvntApi
) {
    suspend fun listPublishedEvents(category: String? = null): Result<List<EventResponse>> = runCatching {
        api.listEvents(category = category, statusFilter = "PUBLISHED")
    }

    suspend fun listAllOrganizerEvents(organizerId: String): Result<List<EventResponse>> = runCatching {
        api.listMyEvents(skip = 0, limit = 100)
    }.recoverCatching {
        api.listEvents(statusFilter = "", skip = 0, limit = 100)
            .filter { event -> event.organizerId == organizerId }
    }.mapNetworkError()

    suspend fun getEvent(id: String): Result<EventResponse> = runCatching {
        api.getEvent(id)
    }

    suspend fun createEvent(request: EventCreateRequest): Result<EventResponse> = runCatching {
        api.createEvent(request)
    }.mapNetworkError()

    suspend fun updateEvent(id: String, request: EventUpdateRequest): Result<EventResponse> = runCatching {
        api.updateEvent(id, request)
    }

    suspend fun publishEvent(id: String): Result<EventResponse> = runCatching {
        api.updateEvent(id, EventUpdateRequest(status = "PUBLISHED"))
    }

    suspend fun deleteEvent(id: String): Result<Unit> = runCatching {
        api.deleteEvent(id)
    }

    suspend fun assignStaff(eventId: String, userId: String): Result<StaffAssignmentResponse> = runCatching {
        api.assignStaff(eventId, StaffAssignmentRequest(userId))
    }
}
