package com.example.jhdkasjhd.ui.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.ui.theme.CoinbaseAccentYellow
import com.example.jhdkasjhd.ui.theme.CoinbaseButtonTextStyle
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseMutedSoft
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusLg
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusMd
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceStrong

@Composable
internal fun EventDetailContent(
    event: EventResponse,
    isOrganizer: Boolean,
    isSaved: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onToggleSave: () -> Unit,
    onRegisterClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        EventDetailBanner(
            event = event,
            onBack = onBack,
            onShare = onShare,
            isSaved = isSaved,
            onToggleSave = onToggleSave
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CoinbaseCanvas)
                .padding(horizontal = CoinbaseSpacing.base)
                .padding(top = CoinbaseSpacing.lg, bottom = CoinbaseSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.lg)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                color = CoinbaseInk,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)) {
                EventDetailInfoRow(
                    icon = Icons.Default.AccessTime,
                    primary = formatEventFullDate(event.dateStart),
                    secondary = formatEventTimeRangeDetailed(event.dateStart, event.dateEnd),
                    tertiary = "Horario mostrado en tu zona local"
                )
                EventDetailInfoRow(
                    icon = Icons.Default.LocationOn,
                    primary = event.location.ifBlank { "Ubicación por confirmar" },
                    secondary = event.category.ifBlank { "Panamá" }
                )
                EventDetailInfoRow(
                    icon = Icons.Default.Person,
                    primary = "Organizado por Quickvnt",
                    secondary = "Categoría ${event.category.ifBlank { "general" }}"
                )
            }

            EventDetailLocationMap(location = event.location)

            EventDetailAttendeesSection(capacity = event.capacity)

            HorizontalDivider(color = CoinbaseHairline)

            EventDetailDescriptionSection(description = event.description)

            EventDetailHighlightsRow(event = event)

            if (hasCustomForm(event) || isEndingSoon(event.dateStart)) {
                Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
                    if (hasCustomForm(event)) {
                        EventDetailNoticeCard(
                            icon = Icons.Default.Description,
                            title = "Formulario de registro",
                            body = "Completa un formulario personalizado al inscribirte."
                        )
                    }
                    if (isEndingSoon(event.dateStart)) {
                        EventDetailNoticeCard(
                            icon = Icons.Default.Info,
                            title = "Cupos limitados",
                            body = "El evento es pronto. Reserva tu lugar cuanto antes."
                        )
                    }
                }
            }

            if (isOrganizer) {
                HorizontalDivider(color = CoinbaseHairline)
                EventDetailOrganizerPanel(
                    onAnalyticsClick = onAnalyticsClick,
                    onScanClick = onScanClick
                )
            }
        }
    }
}

@Composable
private fun EventDetailBanner(
    event: EventResponse,
    onBack: () -> Unit,
    onShare: () -> Unit,
    isSaved: Boolean,
    onToggleSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
    ) {
        EventImage(
            bannerUrl = event.bannerUrl,
            category = event.category,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = CoinbaseSpacing.sm, vertical = CoinbaseSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EventDetailFloatingButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = CoinbaseInk,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
                    EventDetailFloatingButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = CoinbaseInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    EventDetailFloatingButton(onClick = onToggleSave) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Quitar de guardados" else "Guardar",
                            tint = if (isSaved) CoinbasePrimary else CoinbaseInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        EventDetailChip(
            text = event.category.ifBlank { "Evento" },
            background = CoinbaseCanvas.copy(alpha = 0.94f),
            contentColor = CoinbaseInk,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(CoinbaseSpacing.base)
        )
    }
}

@Composable
private fun EventDetailInfoRow(
    icon: ImageVector,
    primary: String,
    secondary: String,
    tertiary: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CoinbaseMuted,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = primary,
                style = MaterialTheme.typography.titleMedium,
                color = CoinbaseInk,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodyMedium,
                color = CoinbaseMuted
            )
            if (!tertiary.isNullOrBlank()) {
                Text(
                    text = tertiary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CoinbaseMutedSoft
                )
            }
        }
    }
}

@Composable
private fun EventDetailAttendeesSection(capacity: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = CoinbaseMuted,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Hasta $capacity personas pueden asistir",
                style = MaterialTheme.typography.titleMedium,
                color = CoinbaseInk,
                fontWeight = FontWeight.SemiBold
            )
        }
        EventDetailAvatarRow(count = minOf(capacity, 8))
    }
}

@Composable
private fun EventDetailAvatarRow(count: Int) {
    if (count <= 0) return
    val palette = listOf(
        CoinbasePrimary,
        CoinbaseAccentYellow,
        CoinbaseSemanticUp,
        Color(0xFF9B59FF),
        Color(0xFFE86A17)
    )
    Row(
        modifier = Modifier.padding(start = 34.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .offset(x = (-10 * index).dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(palette[index % palette.size])
                    .border(2.dp, CoinbaseCanvas, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ('A' + (index % 26)).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = CoinbaseOnPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EventDetailDescriptionSection(description: String) {
    var expanded by remember { mutableStateOf(false) }
    val text = description.ifBlank { "Sin descripción disponible." }
    val canExpand = text.length > 180

    Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = CoinbaseInk,
            maxLines = if (expanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis
        )
        if (canExpand) {
            Row(
                modifier = Modifier.clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Leer menos" else "Leer más",
                    style = MaterialTheme.typography.labelLarge,
                    color = CoinbasePrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CoinbasePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EventDetailHighlightsRow(event: EventResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        EventDetailMiniCard(
            icon = Icons.Default.Category,
            label = event.category.ifBlank { "General" },
            modifier = Modifier.weight(1f)
        )
        EventDetailMiniCard(
            icon = Icons.Default.ConfirmationNumber,
            label = StatusLabels.eventStatus(event.status),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EventDetailMiniCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CoinbaseRadiusMd)
            .background(CoinbaseSurfaceSoft)
            .padding(CoinbaseSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CoinbasePrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CoinbaseInk,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EventDetailNoticeCard(
    icon: ImageVector,
    title: String,
    body: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusLg)
            .background(CoinbaseSurfaceSoft)
            .padding(CoinbaseSpacing.base),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CoinbaseRadiusMd)
                .background(CoinbasePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CoinbasePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = CoinbaseInk,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = CoinbaseMuted
            )
        }
    }
}

@Composable
private fun EventDetailOrganizerPanel(
    onAnalyticsClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
        Text(
            text = "Panel del organizador",
            style = MaterialTheme.typography.titleMedium,
            color = CoinbaseInk,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
            EventDetailOrganizerTool(
                icon = Icons.Default.Analytics,
                label = "Analíticas",
                onClick = onAnalyticsClick,
                modifier = Modifier.weight(1f)
            )
            EventDetailOrganizerTool(
                icon = Icons.Default.QrCodeScanner,
                label = "Check-in QR",
                onClick = onScanClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EventDetailOrganizerTool(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CoinbaseRadiusLg)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusLg)
            .background(CoinbaseCanvas)
            .clickable(onClick = onClick)
            .padding(CoinbaseSpacing.base),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CoinbasePrimary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CoinbaseInk,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EventDetailChip(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clip(CoinbaseRadiusPill)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EventDetailFloatingButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(CoinbaseCanvas.copy(alpha = 0.94f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun EventDetailAttendeeBottomBar(
    event: EventResponse,
    onRegisterClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CoinbaseCanvas)
            .border(width = 1.dp, color = CoinbaseHairline)
            .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.base),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "GRATIS",
            style = MaterialTheme.typography.titleLarge,
            color = CoinbaseInk,
            fontWeight = FontWeight.Bold
        )
        Button(
            onClick = onRegisterClick,
            enabled = enabled,
            shape = CoinbaseRadiusMd,
            colors = ButtonDefaults.buttonColors(
                containerColor = CoinbasePrimary,
                contentColor = CoinbaseOnPrimary,
                disabledContainerColor = CoinbaseHairline,
                disabledContentColor = CoinbaseMuted
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(CoinbaseSpacing.xs))
            Text(text = "Registrarse", style = CoinbaseButtonTextStyle)
        }
    }
}

@Composable
internal fun EventDetailOrganizerBottomBar(
    event: EventResponse,
    onAnalyticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CoinbaseCanvas)
            .border(width = 1.dp, color = CoinbaseHairline)
            .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.base),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = StatusLabels.eventStatus(event.status).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = CoinbaseInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${event.capacity} cupos",
                style = MaterialTheme.typography.bodySmall,
                color = CoinbaseMuted
            )
        }
        Button(
            onClick = onAnalyticsClick,
            shape = CoinbaseRadiusMd,
            colors = ButtonDefaults.buttonColors(
                containerColor = CoinbasePrimary,
                contentColor = CoinbaseOnPrimary
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
        ) {
            Text(text = "Analíticas", style = CoinbaseButtonTextStyle)
        }
    }
}
