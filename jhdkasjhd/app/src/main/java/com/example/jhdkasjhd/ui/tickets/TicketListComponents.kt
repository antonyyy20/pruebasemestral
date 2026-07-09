package com.example.jhdkasjhd.ui.tickets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.TicketResponse
import com.example.jhdkasjhd.ui.theme.CoinbaseAccentYellow
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimaryActive
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TicketOuterShape = RoundedCornerShape(14.dp)

@Composable
internal fun PhysicalTicketListCard(
    ticket: TicketResponse,
    event: EventResponse?,
    ticketNumber: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eventTitle = event?.title ?: "Evento Quickvnt"
    val monthLabel = formatTicketMonth(event?.dateStart)
    val timeLabel = formatTicketTime(event?.dateStart)
    val statusLabel = StatusLabels.ticketStatus(ticket.status)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, TicketOuterShape, clip = false)
            .clip(TicketOuterShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.72f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CoinbaseAccentYellow)
                        .padding(horizontal = CoinbaseSpacing.base, vertical = 8.dp)
                ) {
                    Text(
                        text = "BOLETO",
                        style = MaterialTheme.typography.titleMedium,
                        color = CoinbaseInk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(CoinbasePrimary, CoinbasePrimaryActive)
                            )
                        )
                        .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.sm),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nº ${ticketNumber.toString().padStart(4, '0')}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CoinbaseOnPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        text = eventTitle.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleSmall,
                        color = CoinbaseOnPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
                        TicketInfoBox(label = monthLabel)
                        TicketInfoBox(label = timeLabel)
                    }
                }
            }

            TicketVerticalPerforation(
                modifier = Modifier
                    .width(14.dp)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .weight(0.28f)
                    .fillMaxHeight()
                    .background(CoinbaseCanvas)
                    .padding(horizontal = CoinbaseSpacing.sm, vertical = CoinbaseSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BOLETO",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoinbasePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CoinbaseHairline.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = CoinbasePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nº ${ticketNumber.toString().padStart(4, '0')}",
                        style = MaterialTheme.typography.labelLarge,
                        color = CoinbaseInk,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = CoinbaseMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketInfoBox(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = CoinbaseOnPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TicketVerticalPerforation(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(CoinbaseCanvas),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxHeight().width(14.dp)) {
            val notchRadius = 5.dp.toPx()
            val step = notchRadius * 2.2f
            var y = notchRadius
            while (y < size.height) {
                drawCircle(
                    color = Color(0xFFF0F0F0),
                    radius = notchRadius,
                    center = Offset(size.width / 2f, y)
                )
                y += step
            }
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            drawLine(
                color = CoinbaseHairline,
                start = Offset(size.width / 2f, notchRadius),
                end = Offset(size.width / 2f, size.height - notchRadius),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )
        }
    }
}

private fun formatTicketMonth(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "TBD"
    return try {
        val zoned = Instant.parse(isoDate).atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-PA"))
            .format(zoned)
            .uppercase(Locale.getDefault())
    } catch (_: Exception) {
        "TBD"
    }
}

private fun formatTicketTime(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "--:--"
    return try {
        val zoned = Instant.parse(isoDate).atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-PA"))
            .format(zoned)
            .lowercase(Locale.getDefault())
    } catch (_: Exception) {
        "--:--"
    }
}
