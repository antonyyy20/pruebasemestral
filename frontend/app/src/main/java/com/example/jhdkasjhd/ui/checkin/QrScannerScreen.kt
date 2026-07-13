package com.example.jhdkasjhd.ui.checkin

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.core.util.QrCodeUtils
import com.example.jhdkasjhd.ui.components.CoinbaseInlineMessage
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSecondaryButton
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

@Composable
fun QrScannerScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: CheckinViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var scanningEnabled by remember { mutableStateOf(true) }
    var barcodeView by remember { mutableStateOf<BarcodeView?>(null) }

    val processScan = rememberUpdatedState { raw: String ->
        if (scanningEnabled && !uiState.isLoading) {
            val payload = QrCodeUtils.parseTicketPayload(raw)
            when {
                payload == null -> {
                    scanningEnabled = false
                    barcodeView?.pause()
                    viewModel.showError("QR inválido o formato incorrecto")
                }
                payload.eventId != eventId -> {
                    scanningEnabled = false
                    barcodeView?.pause()
                    viewModel.showError("Este ticket no pertenece a este evento")
                }
                else -> {
                    scanningEnabled = false
                    barcodeView?.pause()
                    viewModel.validateQr(eventId, payload.ticketId, payload.qrSignature)
                }
            }
        }
    }

    fun resumeScanning() {
        viewModel.clearMessage()
        scanningEnabled = true
        if (hasCameraPermission && !uiState.isLoading) {
            barcodeView?.resume()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) barcodeView?.resume()
    }

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            barcodeView?.pause()
        } else if (scanningEnabled && uiState.message == null && hasCameraPermission) {
            barcodeView?.resume()
        }
    }

    DisposableEffect(lifecycleOwner, barcodeView, scanningEnabled, uiState.isLoading, hasCameraPermission) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (hasCameraPermission && scanningEnabled && !uiState.isLoading && uiState.message == null) {
                        barcodeView?.resume()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> barcodeView?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            barcodeView?.pause()
        }
    }

    QuickvntScaffold(title = "Registro QR", onBack = onBack) { padding ->
        if (uiState.isLoading) {
            LoadingBox(Modifier.padding(padding))
            return@QuickvntScaffold
        }

        if (!hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(CoinbaseSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Se necesita permiso de cámara para escanear el QR del boleto.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CoinbaseInk,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(CoinbaseSpacing.lg))
                CoinbasePrimaryButton(
                    text = "Permitir cámara",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
            return@QuickvntScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        BarcodeView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
                            decodeContinuous(object : BarcodeCallback {
                                override fun barcodeResult(result: BarcodeResult?) {
                                    result?.text?.let { processScan.value(it) }
                                }

                                override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) = Unit
                            })
                            barcodeView = this
                            resume()
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .border(
                                width = 3.dp,
                                color = CoinbaseOnPrimary,
                                shape = RoundedCornerShape(16.dp)
                            )
                    )
                }

                Text(
                    text = "Apunta al código QR del boleto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoinbaseOnPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(CoinbaseSpacing.lg)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(CoinbaseSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Evento: ${eventId.take(8)}…",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoinbaseMuted
                )

                uiState.message?.let { message ->
                    Spacer(Modifier.height(CoinbaseSpacing.md))
                    CoinbaseInlineMessage(
                        message = message,
                        isError = uiState.isError
                    )
                    Spacer(Modifier.height(CoinbaseSpacing.sm))
                    CoinbaseSecondaryButton(
                        text = "Escanear otro",
                        onClick = ::resumeScanning,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
