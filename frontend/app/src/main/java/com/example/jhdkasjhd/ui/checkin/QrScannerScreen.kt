package com.example.jhdkasjhd.ui.checkin

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.core.util.QrCodeUtils
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
    var torchEnabled by remember { mutableStateOf(false) }
    var scanningEnabled by remember { mutableStateOf(true) }
    var barcodeView by remember { mutableStateOf<BarcodeView?>(null) }

    val processScan = rememberUpdatedState { raw: String ->
        if (!scanningEnabled || uiState.isLoading) return

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
        if (granted) {
            barcodeView?.resume()
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            !hasCameraPermission -> {
                QrCameraPermissionPrompt(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onBack = onBack
                )
            }
            else -> {
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
                    },
                    update = { view ->
                        view.setTorch(torchEnabled)
                    }
                )

                QrViewfinderOverlay()

                QrScannerTopBar(
                    torchEnabled = torchEnabled,
                    onClose = onBack,
                    onToggleTorch = {
                        torchEnabled = !torchEnabled
                        barcodeView?.setTorch(torchEnabled)
                    }
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    QrScannerBottomPanel(
                        eventLabel = "Evento · ${eventId.take(8).uppercase()}…",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                    )
                }

                if (uiState.isLoading || uiState.message != null) {
                    QrScannerResultSheet(
                        message = uiState.message.orEmpty(),
                        isError = uiState.isError,
                        isLoading = uiState.isLoading,
                        onScanAgain = ::resumeScanning
                    )
                }
            }
        }
    }
}
