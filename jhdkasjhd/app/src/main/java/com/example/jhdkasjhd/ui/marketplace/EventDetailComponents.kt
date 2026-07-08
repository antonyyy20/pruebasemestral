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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusLg
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusMd
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusXl
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceStrong

private val EventDetailBackground = CoinbaseSurfaceSoft

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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CoinbaseSpacing.base),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
    ) {
        EventDetailHeroCard(
            event = event,
            onBack = onBack,
            onShare = onShare,
            isSaved = isSaved,
            onToggleSave = onToggleSave
        )

        EventDetailOrganizerRow(category = event.category)

        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineSmall,
            color = CoinbaseInk,
            fontWeight = FontWeight.Bold
        )

        if (isOrganizer) {
            EventDetailOrganizerActionRow(
                onAnalyticsClick = onAnalyticsClick,
                onScanClick = onScanClick
            )
        } else {
            EventDetailAttendeeActionRow(
                onRegisterClick = onRegisterClick,
                onShare = onShare,
                enabled = event.status == "PUBLISHED"
            )
        }

        EventDetailInfoCard(
            icon = Icons.Default.AccessTime,
            iconBackground = CoinbasePrimary.copy(alpha = 0.12f),
            iconTint = CoinbasePrimary,
            title = formatEventDayOfWeek(event.dateStart),
            subtitle = formatEventTimeRange(event.dateStart, event.dateEnd),
            trailing = {
                EventDetailChip(
                    text = "Gratis",
                    background = CoinbaseSemanticUp.copy(alpha = 0.12f),
                    contentColor = CoinbaseSemanticUp
                )
            }
        )

        EventDetailInfoCard(
            icon = Icons.Default.LocationOn,
            iconBackground = CoinbaseAccentYellow.copy(alpha = 0.18f),
            iconTint = CoinbaseAccentYellow,
            title = event.location.ifBlank { "Ubicación por confirmar" },
            subtitle = "Regístrate para ver detalles completos del lugar",
            footer = { EventDetailLocationPreview(location = event.location) }
        )

        EventDetailInfoCard(
            icon = Icons.Default.Category,
            iconBackground = CoinbasePrimary.copy(alpha = 0.12f),
            iconTint = CoinbasePrimary,
            title = event.category.ifBlank { "General" },
            subtitle = "Categoría del evento"
        )

        EventDetailInfoCard(
            icon = Icons.Default.Groups,
            iconBackground = CoinbasePrimary.copy(alpha = 0.12f),
            iconTint = CoinbasePrimary,
            title = "${event.capacity} cupos disponibles",
            subtitle = "Capacidad máxima del evento",
            trailing = {
                EventDetailChip(
                    text = StatusLabels.eventStatus(event.status),
                    background = CoinbaseSurfaceStrong,
                    contentColor = CoinbaseInk
                )
            }
        )

        EventDetailOverviewCard(description = event.description)

        if (hasCustomForm(event)) {
            EventDetailInfoCard(
                icon = Icons.Default.Description,
                iconBackground = CoinbasePrimary.copy(alpha = 0.12f),
                iconTint = CoinbasePrimary,
                title = "Formulario de registro",
                subtitle = "Deberás completar un formulario personalizado al inscribirte."
            )
        }

        if (isEndingSoon(event.dateStart)) {
            EventDetailInfoCard(
                icon = Icons.Default.Info,
                iconBackground = CoinbaseAccentYellow.copy(alpha = 0.18f),
                iconTint = CoinbaseAccentYellow,
                title = "Cupos limitados",
                subtitle = "El evento es pronto. Asegura tu lugar antes de que se agoten."
            )
        }

        if (isOrganizer) {
            EventDetailInfoCard(
                icon = Icons.Default.Analytics,
                iconBackground = CoinbasePrimary.copy(alpha = 0.12f),
                iconTint = CoinbasePrimary,
                title = "Panel del organizador",
                subtitle = "Consulta analíticas o escanea QR para validar ingresos.",
                modifier = Modifier.clickable(onClick = onAnalyticsClick)
            )
        }

        Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))
    }
}

@Composable
private fun EventDetailHeroCard(
    event: EventResponse,
    onBack: () -> Unit,
    onShare: () -> Unit,
    isSaved: Boolean,
    onToggleSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .clip(CoinbaseRadiusXl)
    ) {
        EventImage(
            bannerUrl = event.bannerUrl,
            category = event.category,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(CoinbaseSpacing.base),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EventDetailIconCircle(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = CoinbaseInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
                    EventDetailIconCircle(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = CoinbaseInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    EventDetailIconCircle(onClick = onToggleSave) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Quitar de guardados" else "Guardar",
                            tint = if (isSaved) CoinbasePrimary else CoinbaseInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
                EventDetailChip(
                    text = event.category.ifBlank { "Evento" },
                    background = CoinbaseCanvas.copy(alpha = 0.92f),
                    contentColor = CoinbaseInk
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = CoinbaseOnPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CoinbaseOnPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = event.location.ifBlank { "Panamá" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseOnPrimary.copy(alpha = 0.92f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = CoinbaseOnPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatEventDetailDateTime(event.dateStart),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseOnPrimary.copy(alpha = 0.92f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailOrganizerRow(category: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CoinbasePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = CoinbasePrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Quickvnt",
                    style = MaterialTheme.typography.titleSmall,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CoinbaseMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = category.ifBlank { "Organizador verificado" },
                style = MaterialTheme.typography.bodySmall,
                color = CoinbaseMuted
            )
        }
    }
}

@Composable
private fun EventDetailAttendeeActionRow(
    onRegisterClick: () -> Unit,
    onShare: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Button(
            onClick = onRegisterClick,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            shape = CoinbaseRadiusPill,
            colors = ButtonDefaults.buttonColors(
                containerColor = CoinbasePrimary,
                contentColor = CoinbaseOnPrimary,
                disabledContainerColor = CoinbaseHairline,
                disabledContentColor = CoinbaseMuted
            ),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(CoinbaseSpacing.xs))
            Text(text = "Registrarme", style = CoinbaseButtonTextStyle)
        }
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f),
            shape = CoinbaseRadiusPill,
            border = androidx.compose.foundation.BorderStroke(1.dp, CoinbaseHairline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoinbaseInk),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(CoinbaseSpacing.xs))
            Text(text = "Compartir", style = CoinbaseButtonTextStyle)
        }
    }
}

@Composable
private fun EventDetailOrganizerActionRow(
    onAnalyticsClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Button(
            onClick = onAnalyticsClick,
            modifier = Modifier.weight(1f),
            shape = CoinbaseRadiusPill,
            colors = ButtonDefaults.buttonColors(
                containerColor = CoinbasePrimary,
                contentColor = CoinbaseOnPrimary
            ),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(CoinbaseSpacing.xs))
            Text(text = "Analíticas", style = CoinbaseButtonTextStyle)
        }
        OutlinedButton(
            onClick = onScanClick,
            modifier = Modifier.weight(1f),
            shape = CoinbaseRadiusPill,
            border = androidx.compose.foundation.BorderStroke(1.dp, CoinbaseHairline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoinbaseInk),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(CoinbaseSpacing.xs))
            Text(text = "Check-in", style = CoinbaseButtonTextStyle)
        }
    }
}

@Composable
private fun EventDetailInfoCard(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusLg)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusLg)
            .background(CoinbaseCanvas)
            .padding(CoinbaseSpacing.base),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CoinbaseRadiusMd)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseMuted
                    )
                }
            }
            trailing?.invoke()
        }
        footer?.invoke()
    }
}

@Composable
private fun EventDetailOverviewCard(description: String) {
    var expanded by remember { mutableStateOf(false) }
    val text = description.ifBlank { "Sin descripción disponible." }
    val canExpand = text.length > 140

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusLg)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusLg)
            .background(CoinbaseCanvas)
            .padding(CoinbaseSpacing.base),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CoinbaseRadiusMd)
                    .background(CoinbasePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = CoinbasePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Acerca del evento",
                    style = MaterialTheme.typography.titleMedium,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoinbaseMuted,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
private fun EventDetailLocationPreview(location: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(CoinbaseRadiusMd)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CoinbasePrimary.copy(alpha = 0.15f),
                        CoinbaseSurfaceStrong
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = CoinbasePrimary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = location.ifBlank { "Panamá" },
                style = MaterialTheme.typography.labelLarge,
                color = CoinbaseInk,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventDetailChip(
    text: String,
    background: Color,
    contentColor: Color
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(CoinbaseRadiusPill)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EventDetailIconCircle(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(CoinbaseCanvas.copy(alpha = 0.92f))
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
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CoinbaseRadiusMd)
                    .background(CoinbasePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = CoinbasePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Desde $0",
                    style = MaterialTheme.typography.titleMedium,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatEventDetailDateTime(event.dateStart),
                    style = MaterialTheme.typography.bodySmall,
                    color = CoinbaseMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Button(
            onClick = onRegisterClick,
            enabled = enabled,
            shape = CoinbaseRadiusPill,
            colors = ButtonDefaults.buttonColors(
                containerColor = CoinbasePrimary,
                contentColor = CoinbaseOnPrimary,
                disabledContainerColor = CoinbaseHairline,
                disabledContentColor = CoinbaseMuted
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(text = "Obtener boletos", style = CoinbaseButtonTextStyle)
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
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CoinbaseRadiusMd)
                    .background(CoinbasePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CoinbasePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = StatusLabels.eventStatus(event.status),
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
        }
        Button(
            onClick = onAnalyticsClick,
            shape = CoinbaseRadiusPill,
            colors = ButtonDefaults.buttonColors(
                containerColor = CoinbasePrimary,
                contentColor = CoinbaseOnPrimary
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(text = "Analíticas", style = CoinbaseButtonTextStyle)
        }
    }
}
