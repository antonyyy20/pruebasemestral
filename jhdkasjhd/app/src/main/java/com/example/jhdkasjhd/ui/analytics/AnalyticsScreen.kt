package com.example.jhdkasjhd.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseKpiCard
import com.example.jhdkasjhd.ui.components.CoinbaseProgressBar
import com.example.jhdkasjhd.ui.components.CoinbaseSectionTitle
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun AnalyticsScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) { viewModel.loadAnalytics(eventId) }

    QuickvntScaffold(title = "Dashboard Analítico", onBack = onBack) { padding ->
        when {
            uiState.isLoading -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding)) {
                viewModel.loadAnalytics(eventId)
            }
            uiState.analytics != null -> {
                val data = uiState.analytics!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(CoinbaseSpacing.base),
                    verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
                ) {
                    CoinbaseSectionTitle("Indicadores en tiempo real")
                    CoinbaseKpiCard("Capacidad total", data.capacity.toString())
                    CoinbaseKpiCard("Registrados", "${data.totalRegistered} / ${data.capacity}")
                    CoinbaseKpiCard("Ingresos", data.totalCheckedIn.toString())
                    CoinbaseKpiCard("Ocupación", "${data.occupancyRatePercent}%")
                    CoinbaseKpiCard("Tasa de asistencia", "${data.attendanceRatePercent}%")
                    CoinbaseKpiCard("Estado del evento", StatusLabels.eventStatus(data.status))

                    Text("Ocupación", style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
                    CoinbaseProgressBar(
                        progress = (data.occupancyRatePercent / 100.0).toFloat()
                    )

                    Text("Asistencia (ingresos / registrados)", style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
                    CoinbaseProgressBar(
                        progress = (data.attendanceRatePercent / 100.0).toFloat()
                    )

                    Spacer(Modifier.height(CoinbaseSpacing.xs))
                    Text(
                        "Monto recaudado y conversión de boletos estarán disponibles en la fase de pagos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoinbaseMuted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
