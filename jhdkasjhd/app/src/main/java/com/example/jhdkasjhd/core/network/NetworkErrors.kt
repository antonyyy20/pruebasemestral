package com.example.jhdkasjhd.core.network

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrors {

    fun userMessage(throwable: Throwable): String {
        val root = generateSequence(throwable) { it.cause }.last()
        return when (root) {
            is UnknownHostException -> {
                "Sin conexión al servidor. Revisa tu Internet o reinicia el emulador/dispositivo."
            }
            is SocketTimeoutException -> {
                "El servidor tardó demasiado en responder. Render puede estar iniciando; intenta de nuevo."
            }
            is IOException -> {
                if (root.message.orEmpty().contains("Canceled", ignoreCase = true)) {
                    "La solicitud fue cancelada."
                } else {
                    "Error de red: ${root.message ?: "comprueba tu conexión"}"
                }
            }
            is HttpException -> {
                when (root.code()) {
                    401 -> parseApiError(root) ?: "Sesión expirada. Inicia sesión de nuevo."
                    403 -> parseApiError(root) ?: "No autorizado. Inicia sesión de nuevo."
                    400 -> parseApiError(root) ?: "Solicitud inválida."
                    404 -> "Recurso no encontrado."
                    500, 502, 503, 504 -> "El servidor no está disponible. Intenta en unos segundos."
                    else -> "Error del servidor (${root.code()})."
                }
            }
            else -> throwable.message ?: "Ocurrió un error inesperado."
        }
    }

    private fun parseApiError(error: HttpException): String? {
        return try {
            error.response()?.errorBody()?.string()
                ?.substringAfter("\"detail\":\"")
                ?.substringBefore("\"")
                ?.replace("\\\"", "\"")
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
