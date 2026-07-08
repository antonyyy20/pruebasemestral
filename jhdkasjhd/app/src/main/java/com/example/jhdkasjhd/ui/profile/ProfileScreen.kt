package com.example.jhdkasjhd.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.core.data.UserSession
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.data.dto.ProfileResponse
import com.example.jhdkasjhd.ui.auth.AuthFormTextField
import com.example.jhdkasjhd.ui.auth.AuthViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseBadge
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbaseInlineMessage
import com.example.jhdkasjhd.ui.components.CoinbaseOutlineButton
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSectionTitle
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimaryActive
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val session by authViewModel.session.collectAsState()
    val storedBio by authViewModel.userBio.collectAsState()
    val profileUiState by authViewModel.profileUiState.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authViewModel.loadProfile()
    }

    LaunchedEffect(session?.name) {
        if (name.isBlank()) {
            session?.name?.takeIf { it.isNotBlank() }?.let { name = it }
        }
    }

    LaunchedEffect(profileUiState.profile?.name) {
        profileUiState.profile?.name?.takeIf { it.isNotBlank() }?.let { name = it }
    }

    LaunchedEffect(storedBio) {
        if (bio.isEmpty() && storedBio.isNotEmpty()) {
            bio = storedBio
        }
    }

    LaunchedEffect(profileUiState.saved) {
        if (profileUiState.saved) {
            delay(3000)
            authViewModel.clearProfileSaved()
        }
    }

    QuickvntScaffold(title = "Perfil") { padding ->
        when {
            profileUiState.isLoading && profileUiState.profile == null && session == null -> {
                LoadingBox(Modifier.padding(padding))
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(CoinbaseSurfaceSoft)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = CoinbaseSpacing.base,
                        end = CoinbaseSpacing.base,
                        top = CoinbaseSpacing.base,
                        bottom = CoinbaseSpacing.base
                    ),
                    verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.lg)
                ) {
                    item {
                        ProfileHeroCard(
                            session = session,
                            profile = profileUiState.profile,
                            displayName = name.ifBlank { session?.name.orEmpty() }
                        )
                    }

                    item {
                        CoinbaseSectionTitle(text = "Editar perfil")
                        Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
                            AuthFormTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Nombre"
                            )
                            AuthFormTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = "Bio",
                                singleLine = false
                            )
                            Text(
                                text = "Cuéntale a otros asistentes quién eres y qué eventos te interesan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CoinbaseMuted
                            )
                        }
                    }

                    profileUiState.error?.let { message ->
                        item {
                            CoinbaseInlineMessage(message = message, isError = true)
                        }
                    }

                    if (profileUiState.saved) {
                        item {
                            CoinbaseInlineMessage(
                                message = "Perfil actualizado correctamente.",
                                isError = false
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CoinbaseSurfaceSoft)
                        .padding(horizontal = CoinbaseSpacing.base)
                        .padding(bottom = CoinbaseSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
                ) {
                    CoinbasePrimaryButton(
                        text = "Guardar cambios",
                        onClick = { authViewModel.updateProfile(name, bio) },
                        enabled = name.isNotBlank() && session != null,
                        loading = profileUiState.isSaving,
                        large = true
                    )
                    CoinbaseOutlineButton(
                        text = "Cerrar sesión",
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    session: UserSession?,
    profile: ProfileResponse?,
    displayName: String
) {
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty().ifBlank { "?" }
    val isOrganizer = profile?.role == "ORGANIZER" || session?.isOrganizer == true

    CoinbaseFeatureCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CoinbasePrimary, CoinbasePrimaryActive)
                        )
                    )
                    .border(2.dp, CoinbaseCanvas, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CoinbaseOnPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
            ) {
                Text(
                    text = displayName.ifBlank { "Usuario Quickvnt" },
                    style = MaterialTheme.typography.titleLarge,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isOrganizer) {
                        "Gestiona y publica tus eventos"
                    } else {
                        "Descubre y reserva experiencias"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoinbaseMuted
                )
                CoinbaseBadge(
                    text = if (isOrganizer) "Organizador" else "Asistente"
                )
            }
        }
    }
}
