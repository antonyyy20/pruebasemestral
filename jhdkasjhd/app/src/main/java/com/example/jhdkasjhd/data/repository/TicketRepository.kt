package com.example.jhdkasjhd.data.repository

import com.example.jhdkasjhd.core.network.QuickvntApi
import com.example.jhdkasjhd.data.dto.AnalyticsResponse
import com.example.jhdkasjhd.data.dto.CheckinRequest
import com.example.jhdkasjhd.data.dto.CheckinResponse
import com.example.jhdkasjhd.data.dto.TicketCreateRequest
import com.example.jhdkasjhd.data.dto.TicketResponse

class TicketRepository(
    private val api: QuickvntApi
) {
    suspend fun registerToEvent(
        eventId: String,
        formResponse: Map<String, Any?>
    ): Result<TicketResponse> = runCatching {
        api.registerToEvent(
            eventId = eventId,
            body = TicketCreateRequest(eventId = eventId, formResponse = formResponse)
        )
    }

    suspend fun myTickets(): Result<List<TicketResponse>> = runCatching {
        api.myTickets()
    }

    suspend fun getTicket(id: String): Result<TicketResponse> = runCatching {
        api.getTicket(id)
    }

    suspend fun validateCheckin(request: CheckinRequest): Result<CheckinResponse> = runCatching {
        api.validateCheckin(request)
    }

    suspend fun getAnalytics(eventId: String): Result<AnalyticsResponse> = runCatching {
        api.getEventAnalytics(eventId)
    }
}
