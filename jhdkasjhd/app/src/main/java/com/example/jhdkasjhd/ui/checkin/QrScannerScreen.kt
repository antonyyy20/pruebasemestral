package com.example.jhdkasjhd.ui.checkin

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.core.util.QrCodeUtils
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbaseInlineMessage
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSecondaryButton
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QrScannerScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: CheckinViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val payload = QrCodeUtils.parseTicketPayload(result.contents)
            if (payload == null) {
                viewModel.showError("QR inválido o formato incorrecto")
            } else if (payload.eventId != eventId) {
                viewModel.showError("Este ticket no pertenece a este evento")
            } else {
                viewModel.validateQr(eventId, payload.ticketId, payload.qrSignature)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scannerLauncher.launch(
                ScanOptions()
                    .setPrompt("Escanea el QR del asistente")
                    .setBeepEnabled(true)
                    .setOrientationLocked(false)
            )
        }
    }

    fun launchScanner() {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> {
                scannerLauncher.launch(
                    ScanOptions()
                        .setPrompt("Escanea el QR del asistente")
                        .setBeepEnabled(true)
                        .setOrientationLocked(false)
                )
            }
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    QuickvntScaffold(title = "Registro QR", onBack = onBack) { padding ->
        if (uiState.isLoading) {
            LoadingBox(Modifier.padding(padding))
            return@QuickvntScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(CoinbaseSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CoinbaseFeatureCard {
                Text(
                    "Validación de acceso",
                    style = MaterialTheme.typography.titleMedium,
                    color = CoinbaseInk,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(CoinbaseSpacing.xs))
                Text(
                    "Evento: ${eventId.take(8)}...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoinbaseMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(CoinbaseSpacing.lg))

            CoinbasePrimaryButton(
                text = "Escanear código QR",
                onClick = { launchScanner() }
            )

            uiState.message?.let { message ->
                Spacer(Modifier.height(CoinbaseSpacing.lg))
                CoinbaseInlineMessage(
                    message = message,
                    isError = uiState.isError
                )
                Spacer(Modifier.height(CoinbaseSpacing.sm))
                CoinbaseSecondaryButton(
                    text = "Escanear otro",
                    onClick = {
                        viewModel.clearMessage()
                        launchScanner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
