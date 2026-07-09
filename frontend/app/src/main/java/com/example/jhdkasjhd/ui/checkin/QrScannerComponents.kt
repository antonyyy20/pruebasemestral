package com.example.jhdkasjhd.ui.checkin

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSecondaryButton
import com.example.jhdkasjhd.ui.theme.CoinbaseAccentYellow
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticDown
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

private val ScannerScrim = Color.Black.copy(alpha = 0.62f)

@Composable
fun QrScannerTopBar(
    torchEnabled: Boolean,
    onClose: () -> Unit,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = CoinbaseSpacing.xs, vertical = CoinbaseSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = CoinbaseOnPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(onClick = onToggleTorch) {
            Icon(
                imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = if (torchEnabled) "Apagar flash" else "Encender flash",
                tint = CoinbaseOnPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun QrViewfinderOverlay(
    modifier: Modifier = Modifier,
    instruction: String = "Escanea el código QR del boleto para validar el ingreso"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineProgress"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val frameSize = minWidth * 0.68f
        val frameTop = maxHeight * 0.26f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameLeft = (size.width - frameSize.toPx()) / 2f
            val frameTopPx = frameTop.toPx()
            val frameSizePx = frameSize.toPx()
            val frameRect = Rect(
                offset = Offset(frameLeft, frameTopPx),
                size = Size(frameSizePx, frameSizePx)
            )
            val cornerRadius = 28.dp.toPx()

            val scrimPath = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(
                    RoundRect(
                        rect = frameRect,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
                fillType = PathFillType.EvenOdd
            }
            drawPath(path = scrimPath, color = ScannerScrim)

            val bracketLength = 42.dp.toPx()
            val bracketStroke = 5.dp.toPx()
            val inset = 6.dp.toPx()

            fun drawCorner(
                start: Offset,
                horizontal: Float,
                vertical: Float,
                color: Color
            ) {
                drawLine(
                    color = color,
                    start = start,
                    end = start + Offset(horizontal, 0f),
                    strokeWidth = bracketStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = start,
                    end = start + Offset(0f, vertical),
                    strokeWidth = bracketStroke,
                    cap = StrokeCap.Round
                )
            }

            drawCorner(
                start = Offset(frameRect.left + inset, frameRect.top + inset),
                horizontal = bracketLength,
                vertical = bracketLength,
                color = CoinbaseSemanticDown
            )
            drawCorner(
                start = Offset(frameRect.right - inset, frameRect.top + inset),
                horizontal = -bracketLength,
                vertical = bracketLength,
                color = CoinbaseAccentYellow
            )
            drawCorner(
                start = Offset(frameRect.left + inset, frameRect.bottom - inset),
                horizontal = bracketLength,
                vertical = -bracketLength,
                color = CoinbasePrimary
            )
            drawCorner(
                start = Offset(frameRect.right - inset, frameRect.bottom - inset),
                horizontal = -bracketLength,
                vertical = -bracketLength,
                color = CoinbaseSemanticUp
            )

            val scanY = frameRect.top + frameRect.height * scanProgress
            drawLine(
                color = CoinbaseOnPrimary.copy(alpha = 0.85f),
                start = Offset(frameRect.left + 24.dp.toPx(), scanY),
                end = Offset(frameRect.right - 24.dp.toPx(), scanY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                blendMode = BlendMode.Screen
            )
        }

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyLarge,
            color = CoinbaseOnPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = frameTop + frameSize + CoinbaseSpacing.lg)
                .padding(horizontal = CoinbaseSpacing.xl)
        )
    }
}

@Composable
fun QrScannerBottomPanel(
    eventLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = CoinbaseSpacing.lg, vertical = CoinbaseSpacing.lg),
        shape = RoundedCornerShape(CoinbaseRadiusPill),
        color = CoinbaseOnPrimary.copy(alpha = 0.96f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = CoinbaseSpacing.lg,
                vertical = CoinbaseSpacing.md
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Validación de acceso",
                style = MaterialTheme.typography.titleSmall,
                color = CoinbaseInk
            )
            Text(
                text = eventLabel,
                style = MaterialTheme.typography.bodySmall,
                color = CoinbaseMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = CoinbaseSpacing.xxs)
            )
        }
    }
}

@Composable
fun QrScannerResultSheet(
    message: String,
    isError: Boolean,
    isLoading: Boolean,
    onScanAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(CoinbaseSpacing.lg),
            shape = RoundedCornerShape(28.dp),
            color = CoinbaseOnPrimary
        ) {
            Column(
                modifier = Modifier.padding(CoinbaseSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.md)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = CoinbasePrimary)
                    Text(
                        text = "Validando boleto…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseMuted
                    )
                } else {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isError) CoinbaseSemanticDown else CoinbaseSemanticUp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        CoinbasePrimaryButton(
                            text = "Escanear otro",
                            onClick = onScanAgain
                        )
                    } else {
                        CoinbaseSecondaryButton(
                            text = "Escanear otro",
                            onClick = onScanAgain,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QrCameraPermissionPrompt(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(CoinbaseSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.lg)
        ) {
            Text(
                text = "Necesitamos acceso a la cámara para escanear códigos QR",
                style = MaterialTheme.typography.titleMedium,
                color = CoinbaseOnPrimary,
                textAlign = TextAlign.Center
            )
            CoinbasePrimaryButton(
                text = "Permitir cámara",
                onClick = onRequestPermission
            )
            CoinbaseSecondaryButton(
                text = "Volver",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
