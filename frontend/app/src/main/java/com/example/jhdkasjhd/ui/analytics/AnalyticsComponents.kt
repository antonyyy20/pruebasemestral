package com.example.jhdkasjhd.ui.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.data.dto.AnalyticsResponse
import com.example.jhdkasjhd.ui.theme.CoinbaseAccentYellow
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseNumberStyle
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusMd
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft

@Composable
internal fun AnalyticsTopBar(
    onBack: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CoinbaseSpacing.xs, vertical = CoinbaseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = CoinbaseInk
            )
        }
        Text(
            text = "Actividad del evento",
            style = MaterialTheme.typography.titleMedium,
            color = CoinbaseInk,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onInfoClick) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información",
                tint = CoinbaseInk
            )
        }
    }
}

@Composable
internal fun AnalyticsFilterRow(
    selectedMode: AnalyticsViewMode,
    onModeSelected: (AnalyticsViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(CoinbaseRadiusPill)
                    .border(1.dp, CoinbaseHairline, CoinbaseRadiusPill)
                    .background(CoinbaseCanvas)
                    .clickable { showModeMenu = true }
                    .padding(horizontal = CoinbaseSpacing.base, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
            ) {
                Text(
                    text = selectedMode.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CoinbaseMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = showModeMenu,
                onDismissRequest = { showModeMenu = false }
            ) {
                AnalyticsViewMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.label) },
                        onClick = {
                            onModeSelected(mode)
                            showModeMenu = false
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = { /* reservado para filtros avanzados */ },
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.dp, CoinbaseHairline, CircleShape)
                .background(CoinbaseCanvas)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filtros",
                tint = CoinbaseInk,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun AnalyticsHeroMetrics(
    data: AnalyticsResponse,
    chartLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xl)
        ) {
            AnalyticsHeroMetric(
                value = data.totalRegistered.toString(),
                label = "Total registrados"
            )
            AnalyticsHeroMetric(
                value = data.totalCheckedIn.toString(),
                label = "Total ingresos"
            )
        }

        Row(
            modifier = Modifier
                .clip(CoinbaseRadiusPill)
                .clickable { }
                .padding(horizontal = CoinbaseSpacing.sm, vertical = CoinbaseSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = chartLabel,
                style = MaterialTheme.typography.labelLarge,
                color = CoinbaseInk,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = CoinbaseMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AnalyticsHeroMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = CoinbaseNumberStyle.copy(
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                fontWeight = FontWeight.Bold
            ),
            color = CoinbaseInk
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CoinbaseMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun AnalyticsBarChartSection(
    model: AnalyticsChartModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                model.yAxisSteps.reversed().forEach { step ->
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = CoinbaseMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(Modifier.width(CoinbaseSpacing.xs))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                AnalyticsBarChartCanvas(
                    bars = model.bars,
                    maxValue = model.maxValue,
                    yAxisSteps = model.yAxisSteps,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            model.bars.forEach { bar ->
                Text(
                    text = bar.shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = CoinbaseMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AnalyticsBarChartCanvas(
    bars: List<AnalyticsChartBar>,
    maxValue: Int,
    yAxisSteps: List<Int>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (bars.isEmpty() || maxValue <= 0) return@Canvas

        val chartTop = 8f
        val chartBottom = size.height - 8f
        val chartHeight = chartBottom - chartTop
        val slotWidth = size.width / bars.size
        val barWidth = slotWidth * 0.42f

        yAxisSteps.forEach { step ->
            val y = chartBottom - (step.toFloat() / maxValue.toFloat()) * chartHeight
            drawLine(
                color = CoinbaseHairline,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }

        bars.forEachIndexed { index, bar ->
            val fraction = (bar.value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            val barHeight = chartHeight * fraction
            val centerX = slotWidth * index + slotWidth / 2f
            val left = centerX - barWidth / 2f
            val top = chartBottom - barHeight

            drawRoundRect(
                color = bar.color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight.coerceAtLeast(if (bar.value > 0) 6f else 0f)),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsLegendGrid(
    items: List<AnalyticsLegendItem>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
        maxItemsInEachRow = 2
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.width(150.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(item.color)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = CoinbaseInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun AnalyticsHighlightCard(
    percentLabel: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusMd)
            .background(CoinbaseSurfaceSoft)
            .padding(CoinbaseSpacing.base),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xxs)
        ) {
            Text(
                text = percentLabel,
                style = CoinbaseNumberStyle.copy(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                    fontWeight = FontWeight.Bold
                ),
                color = CoinbaseInk
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CoinbaseMuted
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(CoinbaseAccentYellow),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✦",
                style = MaterialTheme.typography.titleLarge,
                color = CoinbaseOnPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun AnalyticsStatsRow(
    data: AnalyticsResponse,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        AnalyticsMiniStat(
            label = "Capacidad",
            value = data.capacity.toString(),
            modifier = Modifier.weight(1f)
        )
        AnalyticsMiniStat(
            label = "Disponibles",
            value = (data.capacity - data.totalRegistered).coerceAtLeast(0).toString(),
            modifier = Modifier.weight(1f)
        )
        AnalyticsMiniStat(
            label = "Pendientes",
            value = (data.totalRegistered - data.totalCheckedIn).coerceAtLeast(0).toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AnalyticsMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CoinbaseRadiusMd)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusMd)
            .background(CoinbaseCanvas)
            .padding(CoinbaseSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = CoinbasePrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CoinbaseMuted
        )
    }
}

@Composable
internal fun AnalyticsInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sobre estas métricas",
                fontWeight = FontWeight.Bold,
                color = CoinbaseInk
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
                Text(
                    "Registrados: boletos activos del evento.",
                    color = CoinbaseMuted
                )
                Text(
                    "Ingresos: asistentes que ya hicieron check-in con QR.",
                    color = CoinbaseMuted
                )
                Text(
                    "Ocupación: registrados sobre la capacidad total.",
                    color = CoinbaseMuted
                )
                Text(
                    "Asistencia: ingresos sobre registrados.",
                    color = CoinbaseMuted
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = CoinbasePrimary, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = CoinbaseCanvas
    )
}
