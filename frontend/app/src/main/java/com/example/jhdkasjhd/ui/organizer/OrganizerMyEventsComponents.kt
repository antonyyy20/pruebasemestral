package com.example.jhdkasjhd.ui.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.marketplace.EventImage
import com.example.jhdkasjhd.ui.marketplace.formatEventDate
import com.example.jhdkasjhd.ui.marketplace.formatEventTimeRange
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusLg
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusMd
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticDown
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft
import java.time.Instant

internal enum class OrganizerEventFilter(val label: String) {
    ALL("Todos"),
    DRAFT("Borrador"),
    PUBLISHED("Publicado"),
    CLOSED("Cerrado"),
    CANCELLED("Cancelado")
}

internal data class OrganizerEventsSummary(
    val total: Int,
    val drafts: Int,
    val published: Int
)

internal fun summarizeOrganizerEvents(events: List<EventResponse>): OrganizerEventsSummary {
    return OrganizerEventsSummary(
        total = events.size,
        drafts = events.count { it.status.equals("DRAFT", ignoreCase = true) },
        published = events.count { it.status.equals("PUBLISHED", ignoreCase = true) }
    )
}

internal fun filterOrganizerEvents(
    events: List<EventResponse>,
    query: String,
    statusFilter: OrganizerEventFilter
): List<EventResponse> {
    val normalizedQuery = query.trim().lowercase()
    return events
        .filter { event ->
            val matchesQuery = normalizedQuery.isBlank() ||
                event.title.lowercase().contains(normalizedQuery) ||
                event.location.lowercase().contains(normalizedQuery) ||
                event.category.lowercase().contains(normalizedQuery)
            val matchesStatus = when (statusFilter) {
                OrganizerEventFilter.ALL -> true
                OrganizerEventFilter.DRAFT -> event.status.equals("DRAFT", ignoreCase = true)
                OrganizerEventFilter.PUBLISHED -> event.status.equals("PUBLISHED", ignoreCase = true)
                OrganizerEventFilter.CLOSED -> event.status.equals("CLOSED", ignoreCase = true)
                OrganizerEventFilter.CANCELLED -> event.status.equals("CANCELLED", ignoreCase = true)
            }
            matchesQuery && matchesStatus
        }
        .sortedBy { event ->
            runCatching { Instant.parse(event.dateStart) }.getOrNull()
        }
}

@Composable
internal fun MyEventsCreateFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = CoinbasePrimary,
        contentColor = CoinbaseOnPrimary,
        shape = CoinbaseRadiusPill
    ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(CoinbaseSpacing.xs))
        Text(
            text = "Crear evento",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun MyEventsHeader(
    summary: OrganizerEventsSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
    ) {
        Text(
            text = "Panel del organizador",
            style = MaterialTheme.typography.labelMedium,
            color = CoinbaseMuted
        )
        Text(
            text = "Mis eventos",
            style = MaterialTheme.typography.headlineSmall,
            color = CoinbaseInk,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = buildString {
                append("${summary.total} evento${if (summary.total == 1) "" else "s"}")
                if (summary.drafts > 0) append(" · ${summary.drafts} borrador${if (summary.drafts == 1) "" else "es"}")
                if (summary.published > 0) append(" · ${summary.published} publicado${if (summary.published == 1) "" else "s"}")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = CoinbaseMuted
        )
    }
}

@Composable
internal fun MyEventsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusPill)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusPill)
            .background(CoinbaseCanvas)
            .padding(horizontal = CoinbaseSpacing.base, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = CoinbaseMuted,
            modifier = Modifier.size(20.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = CoinbaseInk,
                fontWeight = FontWeight.Medium
            ),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Buscar por título, lugar o categoría",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseMuted
                    )
                }
                inner()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MyEventsStatusFilters(
    selected: OrganizerEventFilter,
    onSelected: (OrganizerEventFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
    ) {
        OrganizerEventFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Text(
                text = filter.label,
                modifier = Modifier
                    .clip(CoinbaseRadiusPill)
                    .background(if (isSelected) CoinbasePrimary else CoinbaseSurfaceSoft)
                    .clickable { onSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) CoinbaseOnPrimary else CoinbaseInk,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
internal fun OrganizerEventCard(
    event: EventResponse,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit,
    onAnalytics: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusLg)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusLg)
            .background(CoinbaseCanvas)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.9f)
        ) {
            EventImage(
                bannerUrl = event.bannerUrl,
                category = event.category,
                modifier = Modifier.fillMaxSize()
            )
            OrganizerEventStatusBadge(
                status = event.status,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(CoinbaseSpacing.sm)
            )
            OrganizerEventCategoryChip(
                category = event.category,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(CoinbaseSpacing.sm)
            )
        }

        Column(
            modifier = Modifier.padding(CoinbaseSpacing.base),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = CoinbaseInk,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            OrganizerEventMetaRow(
                icon = Icons.Default.CalendarMonth,
                text = "${formatEventDate(event.dateStart)} · ${formatEventTimeRange(event.dateStart, event.dateEnd)}"
            )
            OrganizerEventMetaRow(
                icon = Icons.Default.LocationOn,
                text = event.location
            )
            OrganizerEventMetaRow(
                icon = Icons.Default.Groups,
                text = "Capacidad: ${event.capacity} personas"
            )

            if (event.status.equals("DRAFT", ignoreCase = true)) {
                CoinbasePrimaryButton(
                    text = "Publicar evento",
                    onClick = onPublish,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(color = CoinbaseHairline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OrganizerEventAction(
                    icon = Icons.Default.Edit,
                    label = "Editar",
                    onClick = onEdit
                )
                OrganizerEventAction(
                    icon = Icons.Default.BarChart,
                    label = "Analíticas",
                    onClick = onAnalytics
                )
                OrganizerEventAction(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Check-in",
                    onClick = onScan
                )
                OrganizerEventAction(
                    icon = Icons.Default.DeleteOutline,
                    label = "Eliminar",
                    onClick = onDelete,
                    tint = CoinbaseSemanticDown
                )
            }
        }
    }
}

@Composable
private fun OrganizerEventStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (background, content) = organizerStatusColors(status)
    Text(
        text = StatusLabels.eventStatus(status).uppercase(),
        modifier = modifier
            .clip(CoinbaseRadiusMd)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelSmall,
        color = content,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun OrganizerEventCategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = category,
        modifier = modifier
            .clip(CoinbaseRadiusPill)
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelSmall,
        color = CoinbaseOnPrimary,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun OrganizerEventMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CoinbasePrimary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = CoinbaseMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OrganizerEventAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = CoinbaseInk,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CoinbaseRadiusMd)
            .clickable(onClick = onClick)
            .padding(horizontal = CoinbaseSpacing.xs, vertical = CoinbaseSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun DeleteEventDialog(
    eventTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar evento",
                fontWeight = FontWeight.Bold,
                color = CoinbaseInk
            )
        },
        text = {
            Text(
                text = "¿Seguro que quieres eliminar \"$eventTitle\"? Esta acción no se puede deshacer.",
                color = CoinbaseMuted
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = CoinbaseSemanticDown, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = CoinbasePrimary)
            }
        },
        containerColor = CoinbaseCanvas,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun organizerStatusColors(status: String): Pair<Color, Color> {
    return when (status.uppercase()) {
        "DRAFT" -> Color(0xFFFFF4D6) to Color(0xFF8A6100)
        "PUBLISHED" -> Color(0xFFE6F8F0) to CoinbaseSemanticUp
        "CLOSED" -> CoinbaseSurfaceSoft to CoinbaseMuted
        "CANCELLED" -> Color(0xFFFDECEC) to CoinbaseSemanticDown
        else -> CoinbaseSurfaceSoft to CoinbaseInk
    }
}
