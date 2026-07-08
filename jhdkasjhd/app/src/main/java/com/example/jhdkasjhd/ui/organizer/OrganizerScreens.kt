package com.example.jhdkasjhd.ui.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticDown
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

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
    val uiState by viewModel.uiState.collectAsState()

    var eventTitle by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var capacity by rememberSaveable { mutableStateOf("") }
    var bannerUrl by rememberSaveable { mutableStateOf("") }
    var customFormSchema by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var startTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }
    var endDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var endTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }

    var showStartDatePicker by rememberSaveable { mutableStateOf(false) }
    var showStartTimePicker by rememberSaveable { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable { mutableStateOf(false) }
    var showEndTimePicker by rememberSaveable { mutableStateOf(false) }
    var showBannerUrlDialog by rememberSaveable { mutableStateOf(false) }
    var showValidationErrors by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess) {
            viewModel.clearOperationSuccess()
            onCreated()
        }
    }

    val parsedCapacity = capacity.trim().toIntOrNull()
    val parsedSchema = parseCustomFormSchema(customFormSchema)
    val hasValidDates = startDate != null && startTime != null && endDate != null && endTime != null
    val hasValidRange = if (hasValidDates) {
        val start = ZonedDateTime.of(startDate!!, startTime!!, ZoneId.systemDefault())
        val end = ZonedDateTime.of(endDate!!, endTime!!, ZoneId.systemDefault())
        end.isAfter(start)
    } else {
        false
    }

    val isFormValid = eventTitle.isNotBlank() &&
        description.isNotBlank() &&
        category.isNotBlank() &&
        location.isNotBlank() &&
        hasValidDates &&
        hasValidRange &&
        parsedCapacity != null && parsedCapacity > 0 &&
        bannerUrl.isNotBlank() &&
        isValidHttpUrl(bannerUrl) &&
        customFormSchema.isNotBlank() &&
        parsedSchema.isSuccess

    fun submitEvent() {
        showValidationErrors = true
        if (!isFormValid || uiState.isLoading) return

        val schema = parsedSchema.getOrThrow()
        viewModel.createEvent(
            EventCreateRequest(
                title = eventTitle.trim(),
                description = description.trim(),
                category = category.trim(),
                location = location.trim(),
                dateStart = toIsoDateTime(startDate!!, startTime!!),
                dateEnd = toIsoDateTime(endDate!!, endTime!!),
                capacity = parsedCapacity!!,
                bannerUrl = bannerUrl.trim(),
                customFormSchema = schema
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoinbaseCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        CreateEventTopBar(
            onClose = onBack,
            onDone = { submitEvent() },
            doneEnabled = isFormValid,
            isSaving = uiState.isLoading
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            CreateEventHeroSection(
                bannerUrl = bannerUrl,
                onBannerClick = { showBannerUrlDialog = true },
                showError = showValidationErrors && (bannerUrl.isBlank() || !isValidHttpUrl(bannerUrl))
            )

            Column(
                modifier = Modifier.padding(horizontal = CoinbaseSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
            ) {
                CreateEventNameField(
                    value = eventTitle,
                    onValueChange = { eventTitle = it },
                    showError = showValidationErrors && eventTitle.isBlank()
                )

                CreateEventDateTimeRow(
                    label = "Inicio",
                    dateLabel = startDate?.let { formatCreateEventDate(it) } ?: "Fecha",
                    timeLabel = startTime?.let { formatCreateEventTime(it) } ?: "Hora",
                    onDateClick = { showStartDatePicker = true },
                    onTimeClick = { showStartTimePicker = true },
                    showDateError = showValidationErrors && startDate == null,
                    showTimeError = showValidationErrors && startTime == null
                )

                CreateEventDateTimeRow(
                    label = "Fin",
                    dateLabel = endDate?.let { formatCreateEventDate(it) } ?: "Fecha",
                    timeLabel = endTime?.let { formatCreateEventTime(it) } ?: "Hora",
                    onDateClick = { showEndDatePicker = true },
                    onTimeClick = { showEndTimePicker = true },
                    showDateError = showValidationErrors && endDate == null,
                    showTimeError = showValidationErrors && endTime == null
                )

                if (showValidationErrors && hasValidDates && !hasValidRange) {
                    CreateEventFieldError("La fecha de fin debe ser posterior al inicio")
                }

                CreateEventFilledField(
                    value = category,
                    onValueChange = { category = it },
                    placeholder = "Categoría",
                    showError = showValidationErrors && category.isBlank(),
                    errorMessage = "La categoría es obligatoria"
                )

                CreateEventFilledField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "Ubicación",
                    showError = showValidationErrors && location.isBlank(),
                    errorMessage = "La ubicación es obligatoria"
                )

                Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))

                CreateEventSectionTitle("Detalles del evento")

                CreateEventFilledField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Descripción",
                    singleLine = false,
                    minHeight = 120.dp,
                    showError = showValidationErrors && description.isBlank(),
                    errorMessage = "La descripción es obligatoria"
                )

                CreateEventFilledField(
                    value = capacity,
                    onValueChange = { capacity = it.filter { char -> char.isDigit() } },
                    placeholder = "Capacidad",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    showError = showValidationErrors && (parsedCapacity == null || parsedCapacity <= 0),
                    errorMessage = "La capacidad debe ser mayor a 0"
                )

                CreateEventFilledField(
                    value = customFormSchema,
                    onValueChange = { customFormSchema = it },
                    placeholder = "Esquema del formulario (JSON)",
                    singleLine = false,
                    minHeight = 120.dp,
                    showError = showValidationErrors && (customFormSchema.isBlank() || parsedSchema.isFailure),
                    errorMessage = when {
                        customFormSchema.isBlank() -> "El esquema del formulario es obligatorio"
                        else -> parsedSchema.exceptionOrNull()?.message ?: "JSON inválido"
                    }
                )

                uiState.error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseSemanticDown,
                        modifier = Modifier.padding(top = CoinbaseSpacing.xs)
                    )
                }

                Spacer(modifier = Modifier.height(CoinbaseSpacing.xl))
            }
        }
    }

    if (showStartDatePicker) {
        CreateEventDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onConfirm = { startDate = it }
        )
    }

    if (showStartTimePicker) {
        CreateEventTimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { startTime = it }
        )
    }

    if (showEndDatePicker) {
        CreateEventDatePickerDialog(
            initialDate = endDate ?: startDate,
            onDismiss = { showEndDatePicker = false },
            onConfirm = { endDate = it }
        )
    }

    if (showEndTimePicker) {
        CreateEventTimePickerDialog(
            initialTime = endTime ?: startTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { endTime = it }
        )
    }

    if (showBannerUrlDialog) {
        CreateEventBannerUrlDialog(
            currentUrl = bannerUrl,
            onDismiss = { showBannerUrlDialog = false },
            onConfirm = { bannerUrl = it }
        )
    }
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
