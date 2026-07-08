package com.example.jhdkasjhd.ui.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.ui.components.CoinbaseBadge
import com.example.jhdkasjhd.ui.components.CoinbaseDetailRow
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSecondaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSectionTitle
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun MarketplaceScreen(
    onEventClick: (String) -> Unit,
    viewModel: MarketplaceViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    QuickvntScaffold(title = "Eventos") { padding ->
        when {
            uiState.isLoading && uiState.events.isEmpty() -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(
                message = uiState.error!!,
                modifier = Modifier.padding(padding),
                onRetry = { viewModel.loadEvents() }
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(CoinbaseSpacing.base),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
            ) {
                item {
                    CoinbaseSectionTitle("Catálogo de eventos")
                    Text(
                        "Explora eventos publicados y regístrate.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = CoinbaseSpacing.sm)
                    )
                }
                items(uiState.events, key = { it.id }) { event ->
                    EventCard(event = event, onClick = { onEventClick(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventResponse, onClick: () -> Unit) {
    CoinbaseFeatureCard(onClick = onClick) {
        RowHeader(title = event.title, badge = event.category)
        Spacer(Modifier.height(CoinbaseSpacing.xs))
        Text(event.location, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(CoinbaseSpacing.xxs))
        Text(
            "${event.dateStart} → ${event.dateEnd}",
            style = MaterialTheme.typography.bodySmall,
            color = CoinbaseMuted
        )
        Text(
            "Capacidad: ${event.capacity}",
            style = MaterialTheme.typography.bodySmall,
            color = CoinbaseMuted
        )
    }
}

@Composable
private fun RowHeader(title: String, badge: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = CoinbaseInk)
        Spacer(Modifier.height(CoinbaseSpacing.xs))
        CoinbaseBadge(text = badge)
    }
}

@Composable
fun EventDetailScreen(
    eventId: String,
    isOrganizer: Boolean,
    onRegisterClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onScanClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: MarketplaceViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEventDetail(eventId)
    }

    val event = uiState.selectedEvent

    QuickvntScaffold(title = event?.title ?: "Detalle del evento", onBack = onBack) { padding ->
        when {
            uiState.isLoading && event == null -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding)) {
                viewModel.loadEventDetail(eventId)
            }
            event != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(CoinbaseSpacing.base),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
            ) {
                CoinbaseFeatureCard {
                    Text(event.description, style = MaterialTheme.typography.bodyLarge, color = CoinbaseInk)
                }

                CoinbaseFeatureCard {
                    Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
                        CoinbaseDetailRow("Ubicación", event.location)
                        CoinbaseDetailRow("Categoría", event.category)
                        CoinbaseDetailRow("Inicio", event.dateStart)
                        CoinbaseDetailRow("Fin", event.dateEnd)
                        CoinbaseDetailRow("Capacidad", event.capacity.toString())
                        CoinbaseDetailRow("Estado", StatusLabels.eventStatus(event.status))
                    }
                }

                if (!isOrganizer && event.status == "PUBLISHED") {
                    CoinbasePrimaryButton(
                        text = "Registrarme al evento",
                        onClick = onRegisterClick
                    )
                }

                if (isOrganizer) {
                    CoinbasePrimaryButton(text = "Ver analíticas", onClick = onAnalyticsClick)
                    CoinbaseSecondaryButton(
                        text = "Escanear QR de ingreso",
                        onClick = onScanClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
