package com.example.jhdkasjhd.core.util

object StatusLabels {
    fun eventStatus(status: String): String = when (status.uppercase()) {
        "DRAFT" -> "Borrador"
        "PUBLISHED" -> "Publicado"
        "CLOSED" -> "Cerrado"
        "CANCELLED" -> "Cancelado"
        else -> status
    }

    fun ticketStatus(status: String): String = when (status.uppercase()) {
        "REGISTERED" -> "Registrado"
        "CHECKED_IN" -> "Ingresado"
        "CANCELLED" -> "Cancelado"
        else -> status
    }
}
