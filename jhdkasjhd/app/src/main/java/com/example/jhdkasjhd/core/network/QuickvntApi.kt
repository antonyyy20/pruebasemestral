package com.example.jhdkasjhd.core.network

import com.example.jhdkasjhd.data.dto.AnalyticsResponse
import com.example.jhdkasjhd.data.dto.CheckinRequest
import com.example.jhdkasjhd.data.dto.CheckinResponse
import com.example.jhdkasjhd.data.dto.EventCreateRequest
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.EventUpdateRequest
import com.example.jhdkasjhd.data.dto.LoginRequest
import com.example.jhdkasjhd.data.dto.ProfileResponse
import com.example.jhdkasjhd.data.dto.ProfileUpdateRequest
import com.example.jhdkasjhd.data.dto.RefreshRequest
import com.example.jhdkasjhd.data.dto.RegisterRequest
import com.example.jhdkasjhd.data.dto.StaffAssignmentRequest
import com.example.jhdkasjhd.data.dto.StaffAssignmentResponse
import com.example.jhdkasjhd.data.dto.TicketCreateRequest
import com.example.jhdkasjhd.data.dto.TicketResponse
import com.example.jhdkasjhd.data.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QuickvntApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    @GET("users/me")
    suspend fun getProfile(): ProfileResponse

    @PUT("users/me")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): ProfileResponse

    @GET("events")
    suspend fun listEvents(
        @Query("category") category: String? = null,
        @Query("status_filter") statusFilter: String = "PUBLISHED",
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<EventResponse>

    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventResponse

    @POST("events")
    suspend fun createEvent(@Body body: EventCreateRequest): EventResponse

    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body body: EventUpdateRequest
    ): EventResponse

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String)

    @POST("events/{id}/staff")
    suspend fun assignStaff(
        @Path("id") eventId: String,
        @Body body: StaffAssignmentRequest
    ): StaffAssignmentResponse

    @POST("tickets/register/{eventId}")
    suspend fun registerToEvent(
        @Path("eventId") eventId: String,
        @Body body: TicketCreateRequest
    ): TicketResponse

    @GET("tickets/me")
    suspend fun myTickets(): List<TicketResponse>

    @GET("tickets/{id}")
    suspend fun getTicket(@Path("id") id: String): TicketResponse

    @POST("checkin/validate")
    suspend fun validateCheckin(@Body body: CheckinRequest): CheckinResponse

    @GET("analytics/events/{eventId}")
    suspend fun getEventAnalytics(@Path("eventId") eventId: String): AnalyticsResponse
}
