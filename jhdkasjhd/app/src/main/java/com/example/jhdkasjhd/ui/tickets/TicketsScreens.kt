package com.example.jhdkasjhd.ui.tickets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.core.util.FormSchemaParser
import com.example.jhdkasjhd.ui.auth.AuthViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseEmptyState
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSectionTitle
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.components.QuickvntTextField
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun MyTicketsScreen(
    onTicketClick: (String) -> Unit,
    viewModel: TicketsViewModel = quickvntViewModel(),
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by authViewModel.session.collectAsState()

    LaunchedEffect(session?.userId) {
        if (session != null) {
            viewModel.loadMyTickets()
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
            ) {
                itemsIndexed(uiState.tickets, key = { _, ticket -> ticket.id }) { index, ticket ->
                    PhysicalTicketListCard(
                        ticket = ticket,
                        event = uiState.eventsById[ticket.eventId],
                        ticketNumber = index + 1,
                        onClick = { onTicketClick(ticket.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit,
    viewModel: TicketsViewModel = quickvntViewModel(),
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by authViewModel.session.collectAsState()
    val ticket = uiState.selectedTicket
    val event = uiState.selectedEvent

    LaunchedEffect(ticketId) { viewModel.loadTicket(ticketId) }

    when {
        uiState.isLoading && ticket == null -> {
            QuickvntScaffold(title = "Mis boletos", onBack = onBack) { padding ->
                LoadingBox(Modifier.padding(padding))
            }
        }
        uiState.error != null && ticket == null -> {
            QuickvntScaffold(title = "Mis boletos", onBack = onBack) { padding ->
                ErrorMessage(uiState.error!!, Modifier.padding(padding)) {
                    viewModel.loadTicket(ticketId)
                }
            }
        }
        ticket != null && event != null -> {
            val ticketIndex = uiState.tickets.indexOfFirst { it.id == ticket.id }.let { index ->
                if (index >= 0) index + 1 else 1
            }
            val totalTickets = uiState.tickets.size.takeIf { it > 0 } ?: 1

            TicketQrScreen(
                event = event,
                ticket = ticket,
                attendeeName = session?.name.orEmpty(),
                onClose = onBack,
                ticketIndex = ticketIndex,
                totalTickets = totalTickets,
                animateEntry = true
            )
        }
        ticket != null -> {
            QuickvntScaffold(title = "Mis boletos", onBack = onBack) { padding ->
                LoadingBox(Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun RegisterEventScreen(
    eventId: String,
    onSuccess: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TicketsViewModel = quickvntViewModel(),
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by authViewModel.session.collectAsState()
    val formValues = remember { mutableStateMapOf<String, String>() }
    var showTicketQr by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { viewModel.loadEventForRegistration(eventId) }

    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess && uiState.selectedTicket != null && uiState.selectedEvent != null) {
            showTicketQr = true
        }
    }

    val event = uiState.eventForRegistration
    val fields = remember(event) {
        event?.let { FormSchemaParser.parseFields(it.customFormSchema) }.orEmpty()
    }

    Box(Modifier.fillMaxSize()) {
        QuickvntScaffold(title = "Registro al evento", onBack = onBack) { padding ->
            when {
                uiState.isLoading && event == null -> LoadingBox(Modifier.padding(padding))
                uiState.error != null && !showTicketQr -> ErrorMessage(uiState.error!!, Modifier.padding(padding))
                event != null && !showTicketQr -> Column(
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

        val ticket = uiState.selectedTicket
        val registeredEvent = uiState.selectedEvent
        if (showTicketQr && ticket != null && registeredEvent != null) {
            TicketQrScreen(
                event = registeredEvent,
                ticket = ticket,
                attendeeName = session?.name.orEmpty(),
                onClose = {
                    viewModel.clearRegistrationSuccess()
                    onSuccess(ticket.id)
                },
                animateEntry = true
            )
        }
    }
}
