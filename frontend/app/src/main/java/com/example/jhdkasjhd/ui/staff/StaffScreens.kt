package com.example.jhdkasjhd.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.StaffMemberResponse
import com.example.jhdkasjhd.ui.auth.AuthViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseEmptyState
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.components.QuickvntTextField
import com.example.jhdkasjhd.ui.marketplace.formatEventDate
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusLg
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticDown
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun StaffEventsScreen(
    onScan: (String) -> Unit,
    viewModel: StaffEventsViewModel = quickvntViewModel(),
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by authViewModel.session.collectAsState()

    LaunchedEffect(session?.userId) {
        if (session != null) {
            viewModel.loadAssignedEvents()
        }
    }

    QuickvntScaffold(
        title = "Mis eventos"
    ) { padding ->
        when {
            uiState.isLoading -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding))
            uiState.events.isEmpty() -> {
                CoinbaseEmptyState(
                    message = "Sin eventos asignados. Pide al organizador que te cree desde Staff en su evento e inicia sesión (no te registres como asistente).",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(CoinbaseSpacing.base),
                    verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
                ) {
                    items(uiState.events, key = { it.id }) { event ->
                        StaffEventCard(
                            event = event,
                            onScan = { onScan(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffEventCard(
    event: EventResponse,
    onScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusLg)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusLg)
            .background(CoinbaseCanvas)
            .padding(CoinbaseSpacing.base),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.titleMedium,
            color = CoinbaseInk,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = formatEventDate(event.dateStart),
            style = MaterialTheme.typography.bodyMedium,
            color = CoinbaseMuted
        )
        Text(
            text = event.location,
            style = MaterialTheme.typography.bodyMedium,
            color = CoinbaseMuted
        )
        CoinbasePrimaryButton(
            text = "Escanear QR",
            onClick = onScan,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ManageStaffScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: StaffManagementViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(eventId) { viewModel.loadStaff(eventId) }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess) {
            name = ""
            email = ""
            password = ""
            viewModel.clearMessages()
        }
    }

    QuickvntScaffold(
        title = "Gestionar staff",
        onBack = onBack
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(CoinbaseSpacing.base),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
        ) {
            item {
                Text(
                    text = "Crea cuentas de staff con email y contraseña",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoinbaseMuted
                )
                Spacer(Modifier.height(CoinbaseSpacing.sm))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
                    Text(
                        text = "Nueva cuenta de staff",
                        style = MaterialTheme.typography.titleSmall,
                        color = CoinbaseInk,
                        fontWeight = FontWeight.SemiBold
                    )
                    QuickvntTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nombre",
                        modifier = Modifier.fillMaxWidth()
                    )
                    QuickvntTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    QuickvntTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    CoinbasePrimaryButton(
                        text = "Crear y asignar",
                        onClick = {
                            viewModel.createStaff(eventId, name, email, password)
                        },
                        enabled = name.length >= 2 && email.isNotBlank() && password.length >= 6,
                        loading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Los asistentes no pueden ser staff. Solo cuentas con rol STAFF.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoinbaseMuted
                    )
                }
            }

            if (uiState.error != null) {
                item { ErrorMessage(uiState.error!!) }
            }

            item {
                Spacer(Modifier.height(CoinbaseSpacing.sm))
                Text(
                    text = "Staff asignado (${uiState.members.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.isLoading && uiState.members.isEmpty()) {
                item { LoadingBox() }
            } else if (uiState.members.isEmpty()) {
                item {
                    CoinbaseEmptyState(
                        message = "Sin staff. Crea la primera cuenta de staff para este evento."
                    )
                }
            } else {
                items(uiState.members, key = { it.id }) { member ->
                    StaffMemberRow(
                        member = member,
                        onRemove = { viewModel.removeStaff(eventId, member.userId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StaffMemberRow(
    member: StaffMemberResponse,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CoinbaseHairline, RoundedCornerShape(12.dp))
            .background(CoinbaseCanvas)
            .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyLarge,
                color = CoinbaseInk,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = member.role,
                style = MaterialTheme.typography.bodySmall,
                color = CoinbasePrimary
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Eliminar staff",
                tint = CoinbaseSemanticDown
            )
        }
    }
}
