package com.example.jhdkasjhd.ui.organizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.data.dto.EventCreateRequest
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.EventUpdateRequest
import com.example.jhdkasjhd.ui.components.CoinbaseBadge
import com.example.jhdkasjhd.ui.components.CoinbaseEmptyState
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbaseOutlineButton
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSecondaryButton
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.components.QuickvntTextField
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun MyEventsScreen(
    onCreateEvent: () -> Unit,
    onEditEvent: (String) -> Unit,
    onAnalytics: (String) -> Unit,
    onScan: (String) -> Unit,
    viewModel: OrganizerViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyEvents() }

    QuickvntScaffold(
        title = "Mis Eventos",
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEvent,
                containerColor = CoinbasePrimary,
                contentColor = CoinbaseOnPrimary
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.events.isEmpty() -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding)) {
                viewModel.loadMyEvents()
            }
            uiState.events.isEmpty() -> CoinbaseEmptyState(
                message = "Aún no has creado eventos.",
                modifier = Modifier.padding(padding),
                actionLabel = "Crear primer evento",
                onAction = onCreateEvent
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(CoinbaseSpacing.base),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
            ) {
                items(uiState.events, key = { it.id }) { event ->
                    OrganizerEventCard(
                        event = event,
                        onEdit = { onEditEvent(event.id) },
                        onPublish = { viewModel.publishEvent(event.id) },
                        onDelete = { viewModel.deleteEvent(event.id) },
                        onAnalytics = { onAnalytics(event.id) },
                        onScan = { onScan(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizerEventCard(
    event: EventResponse,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit,
    onAnalytics: () -> Unit,
    onScan: () -> Unit
) {
    CoinbaseFeatureCard {
        Text(event.title, style = MaterialTheme.typography.titleMedium, color = CoinbaseInk)
        Spacer(Modifier.height(CoinbaseSpacing.xs))
        CoinbaseBadge(text = StatusLabels.eventStatus(event.status))
        Spacer(Modifier.height(CoinbaseSpacing.xs))
        Text("Capacidad: ${event.capacity}", style = MaterialTheme.typography.bodyMedium)
        Text("${event.dateStart} → ${event.dateEnd}", style = MaterialTheme.typography.bodySmall, color = CoinbaseMuted)

        Spacer(Modifier.height(CoinbaseSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
            CoinbaseOutlineButton(text = "Editar", onClick = onEdit)
            if (event.status == "DRAFT") {
                CoinbasePrimaryButton(text = "Publicar", onClick = onPublish, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(CoinbaseSpacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
            CoinbaseSecondaryButton(text = "Analíticas", onClick = onAnalytics)
            CoinbaseSecondaryButton(text = "Ingreso QR", onClick = onScan)
        }
        Spacer(Modifier.height(CoinbaseSpacing.xs))
        CoinbaseOutlineButton(text = "Eliminar", onClick = onDelete, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: OrganizerViewModel = quickvntViewModel()
) {
    EventFormScreen(
        title = "Crear evento",
        onBack = onBack,
        onSubmit = { request -> viewModel.createEvent(request) },
        onSuccess = onCreated,
        viewModel = viewModel
    )
}

@Composable
fun EditEventScreen(
    eventId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: OrganizerViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    val event = uiState.selectedEvent
    if (event == null && uiState.isLoading) {
        LoadingBox()
        return
    }

    EventFormScreen(
        title = "Editar evento",
        initial = event,
        onBack = onBack,
        onSubmit = { request ->
            viewModel.updateEvent(
                eventId,
                EventUpdateRequest(
                    title = request.title,
                    description = request.description,
                    category = request.category,
                    location = request.location,
                    dateStart = request.dateStart,
                    dateEnd = request.dateEnd,
                    capacity = request.capacity,
                    bannerUrl = request.bannerUrl
                )
            )
        },
        onSuccess = onSaved,
        viewModel = viewModel
    )
}

@Composable
private fun EventFormScreen(
    title: String,
    initial: EventResponse? = null,
    onBack: () -> Unit,
    onSubmit: (EventCreateRequest) -> Unit,
    onSuccess: () -> Unit,
    viewModel: OrganizerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var eventTitle by rememberSaveable { mutableStateOf(initial?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initial?.description.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initial?.category.orEmpty()) }
    var location by rememberSaveable { mutableStateOf(initial?.location.orEmpty()) }
    var dateStart by rememberSaveable { mutableStateOf(initial?.dateStart.orEmpty()) }
    var dateEnd by rememberSaveable { mutableStateOf(initial?.dateEnd.orEmpty()) }
    var capacity by rememberSaveable { mutableStateOf(initial?.capacity?.toString() ?: "100") }
    var bannerUrl by rememberSaveable { mutableStateOf(initial?.bannerUrl.orEmpty()) }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess) {
            viewModel.clearOperationSuccess()
            onSuccess()
        }
    }

    QuickvntScaffold(title = title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(CoinbaseSpacing.base),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
        ) {
            QuickvntTextField(eventTitle, { eventTitle = it }, "Título")
            QuickvntTextField(description, { description = it }, "Descripción", singleLine = false)
            QuickvntTextField(category, { category = it }, "Categoría")
            QuickvntTextField(location, { location = it }, "Ubicación")
            QuickvntTextField(dateStart, { dateStart = it }, "Fecha inicio (ISO 8601)")
            QuickvntTextField(dateEnd, { dateEnd = it }, "Fecha fin (ISO 8601)")
            QuickvntTextField(capacity, { capacity = it }, "Capacidad")
            QuickvntTextField(bannerUrl, { bannerUrl = it }, "URL del banner (opcional)")

            uiState.error?.let { ErrorMessage(it) }

            CoinbasePrimaryButton(
                text = if (uiState.isLoading) "Guardando..." else "Guardar",
                onClick = {
                    onSubmit(
                        EventCreateRequest(
                            title = eventTitle,
                            description = description,
                            category = category,
                            location = location,
                            dateStart = dateStart,
                            dateEnd = dateEnd,
                            capacity = capacity.toIntOrNull() ?: 100,
                            bannerUrl = bannerUrl.ifBlank { null }
                        )
                    )
                },
                enabled = !uiState.isLoading,
                loading = uiState.isLoading
            )
        }
    }
}
