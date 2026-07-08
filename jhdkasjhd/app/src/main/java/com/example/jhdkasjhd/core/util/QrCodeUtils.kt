package com.example.jhdkasjhd.core.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

object QrCodeUtils {

    fun buildTicketPayload(
        ticketId: String,
        eventId: String,
        userId: String,
        qrSignature: String
    ): String {
        return JSONObject()
            .put("ticket_id", ticketId)
            .put("event_id", eventId)
            .put("user_id", userId)
            .put("qr_signature", qrSignature)
            .toString()
    }

    fun parseTicketPayload(raw: String): TicketQrPayload? {
        return try {
            val json = JSONObject(raw.trim())
            TicketQrPayload(
                ticketId = json.getString("ticket_id"),
                eventId = json.getString("event_id"),
                userId = json.getString("user_id"),
                qrSignature = json.getString("qr_signature")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}

data class TicketQrPayload(
    val ticketId: String,
    val eventId: String,
    val userId: String,
    val qrSignature: String
)
