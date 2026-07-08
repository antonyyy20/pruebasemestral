package com.example.jhdkasjhd.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MARKETPLACE = "marketplace"
    const val MARKETPLACE_CATEGORY = "marketplace/category/{categoryName}"
    const val CATEGORIES = "categories"
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val REGISTER_EVENT = "register_event/{eventId}"
    const val MY_TICKETS = "my_tickets"
    const val TICKET_DETAIL = "ticket_detail/{ticketId}"
    const val MY_EVENTS = "my_events"
    const val CREATE_EVENT = "create_event"
    const val EDIT_EVENT = "edit_event/{eventId}"
    const val ANALYTICS = "analytics/{eventId}"
    const val QR_SCANNER = "qr_scanner/{eventId}"
    const val PROFILE = "profile"

    fun eventDetail(eventId: String) = "event_detail/$eventId"
    fun registerEvent(eventId: String) = "register_event/$eventId"
    fun ticketDetail(ticketId: String) = "ticket_detail/$ticketId"
    fun editEvent(eventId: String) = "edit_event/$eventId"
    fun analytics(eventId: String) = "analytics/$eventId"
    fun qrScanner(eventId: String) = "qr_scanner/$eventId"
    fun marketplaceCategory(categoryName: String): String {
        val encoded = URLEncoder.encode(categoryName, StandardCharsets.UTF_8.toString())
        return "marketplace/category/$encoded"
    }
}
