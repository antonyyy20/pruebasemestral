package com.example.jhdkasjhd.ui.profile

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.ui.auth.AuthViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbaseOutlineButton
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSectionTitle
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.components.QuickvntTextField
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val session by authViewModel.session.collectAsState()
    var name by rememberSaveable(session?.name) { mutableStateOf(session?.name.orEmpty()) }

    QuickvntScaffold(title = "Mi Perfil") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(CoinbaseSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
        ) {
            CoinbaseSectionTitle("Quickvnt")

            session?.let { user ->
                CoinbaseFeatureCard {
                    Text("ID", style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
                    Text(user.userId, style = MaterialTheme.typography.bodyMedium, color = CoinbaseInk)
                    Spacer(Modifier.height(CoinbaseSpacing.sm))
                    Text("Rol", style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
                    Text(
                        if (user.isOrganizer) "Organizador" else "Asistente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseInk
                    )
                }

                QuickvntTextField(name, { name = it }, "Nombre")
                CoinbasePrimaryButton(
                    text = "Actualizar perfil (próximamente)",
                    onClick = { },
                    enabled = false
                )
            }

            Spacer(Modifier.weight(1f))

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
