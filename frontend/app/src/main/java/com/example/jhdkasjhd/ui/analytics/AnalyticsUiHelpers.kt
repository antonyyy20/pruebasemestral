package com.example.jhdkasjhd.ui.analytics

import androidx.compose.ui.graphics.Color
import com.example.jhdkasjhd.data.dto.AnalyticsResponse
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp

internal enum class AnalyticsViewMode(val label: String) {
    SUMMARY("Resumen del evento"),
    OCCUPANCY("Ocupación"),
    ATTENDANCE("Asistencia")
}

internal data class AnalyticsChartBar(
    val label: String,
    val shortLabel: String,
    val value: Int,
    val color: Color
)

internal data class AnalyticsLegendItem(
    val label: String,
    val color: Color
)

internal data class AnalyticsChartModel(
    val bars: List<AnalyticsChartBar>,
    val legend: List<AnalyticsLegendItem>,
    val maxValue: Int,
    val yAxisSteps: List<Int>,
    val highlightPercent: Double,
    val highlightLabel: String,
    val highlightDescription: String
)

private val ChartRegistered = CoinbasePrimary
private val ChartCheckedIn = Color(0xFF4DA3FF)
private val ChartPending = Color(0xFF93C5FF)
private val ChartAvailable = CoinbaseHairline
private val ChartCapacity = Color(0xFFD6E4FF)

internal fun buildAnalyticsChartModel(
    data: AnalyticsResponse,
    viewMode: AnalyticsViewMode
): AnalyticsChartModel {
    val pendingCheckIn = (data.totalRegistered - data.totalCheckedIn).coerceAtLeast(0)
    val available = (data.capacity - data.totalRegistered).coerceAtLeast(0)

    val bars = when (viewMode) {
        AnalyticsViewMode.SUMMARY -> listOf(
            AnalyticsChartBar("Capacidad", "Cupos", data.capacity, ChartCapacity),
            AnalyticsChartBar("Registrados", "Reg.", data.totalRegistered, ChartRegistered),
            AnalyticsChartBar("Ingresos", "Ing.", data.totalCheckedIn, ChartCheckedIn),
            AnalyticsChartBar("Disponibles", "Disp.", available, ChartAvailable)
        )
        AnalyticsViewMode.OCCUPANCY -> listOf(
            AnalyticsChartBar("Registrados", "Reg.", data.totalRegistered, ChartRegistered),
            AnalyticsChartBar("Disponibles", "Disp.", available, ChartAvailable)
        )
        AnalyticsViewMode.ATTENDANCE -> listOf(
            AnalyticsChartBar("Registrados", "Reg.", data.totalRegistered, ChartRegistered),
            AnalyticsChartBar("Ingresos", "Ing.", data.totalCheckedIn, ChartCheckedIn),
            AnalyticsChartBar("Pendientes", "Pend.", pendingCheckIn, ChartPending)
        )
    }

    val maxValue = computeChartMax(bars.maxOf { it.value }.coerceAtLeast(1))
    val yAxisSteps = buildYAxisSteps(maxValue)

    val legend = when (viewMode) {
        AnalyticsViewMode.SUMMARY -> listOf(
            AnalyticsLegendItem("Capacidad total", ChartCapacity),
            AnalyticsLegendItem("Registrados", ChartRegistered),
            AnalyticsLegendItem("Ingresos", ChartCheckedIn),
            AnalyticsLegendItem("Cupos disponibles", ChartAvailable)
        )
        AnalyticsViewMode.OCCUPANCY -> listOf(
            AnalyticsLegendItem("Registrados", ChartRegistered),
            AnalyticsLegendItem("Cupos disponibles", ChartAvailable),
            AnalyticsLegendItem("Capacidad total", ChartCapacity),
            AnalyticsLegendItem("Estado", CoinbaseMuted)
        )
        AnalyticsViewMode.ATTENDANCE -> listOf(
            AnalyticsLegendItem("Registrados", ChartRegistered),
            AnalyticsLegendItem("Ingresos", ChartCheckedIn),
            AnalyticsLegendItem("Pendientes de ingreso", ChartPending),
            AnalyticsLegendItem("Tasa de asistencia", CoinbaseSemanticUp)
        )
    }

    val (highlightPercent, highlightLabel, highlightDescription) = when (viewMode) {
        AnalyticsViewMode.SUMMARY -> Triple(
            data.occupancyRatePercent,
            formatPercent(data.occupancyRatePercent),
            "Ocupación del evento"
        )
        AnalyticsViewMode.OCCUPANCY -> Triple(
            data.occupancyRatePercent,
            formatPercent(data.occupancyRatePercent),
            "Cupos ocupados vs capacidad"
        )
        AnalyticsViewMode.ATTENDANCE -> Triple(
            data.attendanceRatePercent,
            formatPercent(data.attendanceRatePercent),
            "Tasa de asistencia promedio"
        )
    }

    return AnalyticsChartModel(
        bars = bars,
        legend = legend,
        maxValue = maxValue,
        yAxisSteps = yAxisSteps,
        highlightPercent = highlightPercent,
        highlightLabel = highlightLabel,
        highlightDescription = highlightDescription
    )
}

internal fun formatPercent(value: Double): String {
    return if (value % 1.0 == 0.0) {
        "${value.toInt()}%"
    } else {
        String.format("%.1f%%", value)
    }
}

private fun computeChartMax(rawMax: Int): Int {
    if (rawMax <= 5) return 5
    val step = when {
        rawMax <= 20 -> 5
        rawMax <= 100 -> 10
        else -> 25
    }
    return ((rawMax + step - 1) / step) * step
}

private fun buildYAxisSteps(maxValue: Int): List<Int> {
    val step = (maxValue / 5).coerceAtLeast(1)
    return (0..5).map { step * it }
}
