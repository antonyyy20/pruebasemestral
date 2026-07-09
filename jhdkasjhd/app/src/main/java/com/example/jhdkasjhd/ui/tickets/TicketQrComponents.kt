package com.example.jhdkasjhd.ui.tickets

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jhdkasjhd.core.util.QrCodeUtils
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.TicketResponse
import com.example.jhdkasjhd.ui.marketplace.EventImage
import com.example.jhdkasjhd.ui.marketplace.formatEventTimeRange
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceStrong
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TicketNameColor = Color(0xFF1C1C3A)
private val TicketScrimColor = Color(0xCC0A0B0D)
private val TicketPerforationColor = Color(0xFFDEE1E6)

data class TicketQrDisplayData(
    val attendeeName: String,
    val eventTitle: String,
    val ticketType: String,
    val dateLine: String,
    val timeLine: String,
    val venue: String,
    val ticketIndex: Int = 1,
    val totalTickets: Int = 1,
    val bannerUrl: String? = null,
    val category: String = ""
)

@Composable
fun TicketQrScreen(
    event: EventResponse,
    ticket: TicketResponse,
    attendeeName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    ticketIndex: Int = 1,
    totalTickets: Int = 1,
    animateEntry: Boolean = false
) {
    val displayData = remember(event, ticket, attendeeName, ticketIndex, totalTickets) {
        TicketQrDisplayData(
            attendeeName = attendeeName.ifBlank { "Asistente" },
            eventTitle = event.title,
            ticketType = buildTicketTypeLabel(event),
            dateLine = formatTicketDateLine(event.dateStart),
            timeLine = formatTicketTimeLine(event.dateStart, event.dateEnd),
            venue = event.location.ifBlank { "Por confirmar" },
            ticketIndex = ticketIndex,
            totalTickets = totalTickets,
            bannerUrl = event.bannerUrl,
            category = event.category
        )
    }

    val qrBitmap: Bitmap? = remember(ticket) {
        val payload = QrCodeUtils.buildTicketPayload(
            ticketId = ticket.id,
            eventId = ticket.eventId,
            userId = ticket.userId,
            qrSignature = ticket.qrSignature
        )
        QrCodeUtils.generateQrBitmap(payload)
    }

    var showContent by remember { mutableStateOf(!animateEntry) }
    LaunchedEffect(animateEntry) {
        if (animateEntry) showContent = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        EventImage(
            bannerUrl = event.bannerUrl,
            category = event.category,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TicketScrimColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TicketQrTopBar(
                title = "Mis boletos",
                onClose = onClose
            )

            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn(animationSpec = tween(280))
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    EventbriteTicketCard(
                        displayData = displayData,
                        qrBitmap = qrBitmap,
                        scrimColor = TicketScrimColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketQrTopBar(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = CoinbaseCanvas,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = CoinbaseCanvas,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = CoinbaseCanvas,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EventbriteTicketCard(
    displayData: TicketQrDisplayData,
    qrBitmap: Bitmap?,
    scrimColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoinbaseCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            qrBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Código QR del boleto",
                    modifier = Modifier.size(220.dp)
                )
            }
        }

        TicketPerforationDivider(
            scrimColor = scrimColor,
            badge = {
                Text(
                    text = "Boleto ${displayData.ticketIndex} de ${displayData.totalTickets}",
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CoinbaseSurfaceStrong)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = CoinbaseMuted,
                    fontSize = 11.sp
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TicketField(
                label = "Nombre",
                value = displayData.attendeeName,
                valueStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TicketNameColor,
                    fontSize = 28.sp,
                    lineHeight = 32.sp
                )
            )
            TicketField(
                label = "Evento",
                value = displayData.eventTitle
            )
            TicketField(
                label = "Boleto",
                value = displayData.ticketType
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TicketDateVenueColumn(
                    label = "Fecha",
                    value = displayData.dateLine,
                    subValue = displayData.timeLine,
                    actionLabel = "Agregar al calendario",
                    onActionClick = { },
                    modifier = Modifier.weight(1f)
                )
                TicketDateVenueColumn(
                    label = "Lugar",
                    value = displayData.venue,
                    actionLabel = "Ver mapa",
                    onActionClick = { openMaps(context, displayData.venue) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        TicketPerforationDivider(scrimColor = scrimColor)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TicketField(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.Medium,
        color = CoinbaseInk
    )
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CoinbaseMuted
        )
        Text(
            text = value,
            style = valueStyle
        )
    }
}

@Composable
private fun TicketDateVenueColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subValue: String? = null,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CoinbaseMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = CoinbaseInk
            )
        )
        if (!subValue.isNullOrBlank()) {
            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = CoinbaseInk,
                lineHeight = 18.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = actionLabel,
            modifier = Modifier.clickable(onClick = onActionClick),
            style = MaterialTheme.typography.bodySmall,
            color = CoinbasePrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TicketPerforationDivider(
    scrimColor: Color,
    modifier: Modifier = Modifier,
    notchRadius: Dp = 10.dp,
    badge: @Composable (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val notchRadiusPx = with(density) { notchRadius.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = TicketPerforationColor,
                start = androidx.compose.ui.geometry.Offset(notchRadiusPx + 4f, centerY),
                end = androidx.compose.ui.geometry.Offset(size.width - notchRadiusPx - 4f, centerY),
                strokeWidth = 2f,
                pathEffect = dashEffect
            )
            drawCircle(
                color = scrimColor,
                radius = notchRadiusPx,
                center = androidx.compose.ui.geometry.Offset(0f, centerY)
            )
            drawCircle(
                color = scrimColor,
                radius = notchRadiusPx,
                center = androidx.compose.ui.geometry.Offset(size.width, centerY)
            )
        }
        badge?.invoke()
    }
}

private fun buildTicketTypeLabel(event: EventResponse): String {
    val timeRange = formatEventTimeRange(event.dateStart, event.dateEnd)
    return if (timeRange.isNotBlank() && timeRange != "Horario por confirmar") {
        "Admisión general · $timeRange"
    } else {
        "Admisión general"
    }
}

private fun formatTicketDateLine(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "Por confirmar"
    return try {
        val zoned = Instant.parse(isoDate).atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.forLanguageTag("es-PA"))
        zoned.format(formatter).replaceFirstChar { it.titlecase(Locale.getDefault()) }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatTicketTimeLine(dateStart: String?, dateEnd: String?): String {
    if (dateStart.isNullOrBlank()) return ""
    return try {
        val zone = ZoneId.systemDefault()
        val startZoned = Instant.parse(dateStart).atZone(zone)
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-PA"))
        val zoneShort = startZoned.format(DateTimeFormatter.ofPattern("z", Locale.getDefault()))
        val startTime = startZoned.format(timeFormatter)
        val endTime = if (!dateEnd.isNullOrBlank()) {
            Instant.parse(dateEnd).atZone(zone).format(timeFormatter)
        } else {
            null
        }
        if (endTime != null) {
            "$startTime $zoneShort – $endTime $zoneShort"
        } else {
            "$startTime $zoneShort"
        }
    } catch (_: Exception) {
        ""
    }
}

private fun openMaps(context: android.content.Context, location: String) {
    val uri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
