package com.example.jhdkasjhd.ui.tickets

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.core.util.FormSchemaParser
import com.example.jhdkasjhd.core.util.QrCodeUtils
import com.example.jhdkasjhd.data.dto.TicketResponse
import com.example.jhdkasjhd.ui.components.CoinbaseEmptyState
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSectionTitle
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.components.QuickvntTextField
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseNumberStyle
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun MyTicketsScreen(
    onTicketClick: (String) -> Unit,
    viewModel: TicketsViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyTickets() }

    QuickvntScaffold(title = "Mis boletos") { padding ->
        when {
            uiState.isLoading && uiState.tickets.isEmpty() -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding)) {
                viewModel.loadMyTickets()
            }
            uiState.tickets.isEmpty() -> CoinbaseEmptyState(
                message = "No tienes boletos registrados",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(CoinbaseSpacing.base),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
            ) {
                items(uiState.tickets, key = { it.id }) { ticket ->
                    TicketCard(ticket) { onTicketClick(ticket.id) }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: TicketResponse, onClick: () -> Unit) {
    CoinbaseFeatureCard(onClick = onClick) {
        Text(
            "Boleto #${ticket.id.take(8)}",
            style = MaterialTheme.typography.titleMedium,
            color = CoinbaseInk
        )
        Text("Evento: ${ticket.eventId.take(8)}...", style = MaterialTheme.typography.bodyMedium)
        Text("Estado: ${StatusLabels.ticketStatus(ticket.status)}", style = CoinbaseNumberStyle.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize))
        Text("Registrado: ${ticket.registeredAt}", style = MaterialTheme.typography.bodySmall, color = CoinbaseMuted)
    }
}

@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit,
    viewModel: TicketsViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ticket = uiState.selectedTicket

    LaunchedEffect(ticketId) { viewModel.loadTicket(ticketId) }

    val qrBitmap: Bitmap? = remember(ticket) {
        ticket?.let {
            val payload = QrCodeUtils.buildTicketPayload(
                ticketId = it.id,
                eventId = it.eventId,
                userId = it.userId,
                qrSignature = it.qrSignature
            )
            QrCodeUtils.generateQrBitmap(payload)
        }
    }

    QuickvntScaffold(title = "Mi boleto", onBack = onBack) { padding ->
        when {
            uiState.isLoading && ticket == null -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding)) {
                viewModel.loadTicket(ticketId)
            }
            ticket != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(CoinbaseSpacing.base),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
            ) {
                CoinbaseFeatureCard {
                    Text("Estado", style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
                    Text(StatusLabels.ticketStatus(ticket.status), style = MaterialTheme.typography.titleMedium, color = CoinbaseInk)
                    Spacer(Modifier.height(CoinbaseSpacing.xs))
                    Text("ID del evento", style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
                    Text(ticket.eventId, style = MaterialTheme.typography.bodyMedium)
                }

                CoinbaseFeatureCard {
                    qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Código QR del boleto",
                            modifier = Modifier
                                .size(260.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    Text(
                        "Presenta este QR en la entrada del evento",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoinbaseMuted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterEventScreen(
    eventId: String,
    onSuccess: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TicketsViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formValues = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(eventId) { viewModel.loadEventForRegistration(eventId) }

    LaunchedEffect(uiState.registrationSuccess) {
        uiState.selectedTicket?.let { onSuccess(it.id) }
    }

    val event = uiState.eventForRegistration
    val fields = remember(event) {
        event?.let { FormSchemaParser.parseFields(it.customFormSchema) }.orEmpty()
    }

    QuickvntScaffold(title = "Registro al evento", onBack = onBack) { padding ->
        when {
            uiState.isLoading && event == null -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding))
            event != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(CoinbaseSpacing.base),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
            ) {
                CoinbaseSectionTitle(event.title)
                Text("Completa el formulario de registro", style = MaterialTheme.typography.bodyMedium)

                fields.forEach { field ->
                    QuickvntTextField(
                        value = formValues[field.key].orEmpty(),
                        onValueChange = { formValues[field.key] = it },
                        label = field.label + if (field.required) " *" else ""
                    )
                }

                if (fields.isEmpty()) {
                    Text("Este evento no requiere información adicional.", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(CoinbaseSpacing.xs))
                CoinbasePrimaryButton(
                    text = if (uiState.isLoading) "Registrando..." else "Confirmar registro",
                    onClick = {
                        val response = formValues.toMap().mapValues { it.value as Any? }
                        viewModel.registerToEvent(eventId, response)
                    },
                    enabled = !uiState.isLoading,
                    loading = uiState.isLoading
                )
            }
        }
    }
}
