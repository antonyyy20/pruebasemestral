package com.example.jhdkasjhd.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.ui.theme.CoinbaseAccentYellow
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimaryActive
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var exitStarted by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
        delay(1700)
        exitStarted = true
        delay(350)
        onFinished()
    }

    val exitAlpha by animateFloatAsState(
        targetValue = if (exitStarted) 0f else 1f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "splashExit"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val outerGlowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerGlowScale"
    )
    val logoPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(exitAlpha)
            .background(CoinbaseCanvas),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(outerGlowScale)
                .alpha(glowAlpha * 0.45f)
                .blur(64.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CoinbaseAccentYellow.copy(alpha = 0.35f),
                            CoinbasePrimary.copy(alpha = 0.12f),
                            CoinbaseCanvas
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(glowScale)
                .alpha(glowAlpha)
                .blur(48.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CoinbasePrimary.copy(alpha = 0.55f),
                            CoinbaseAccentYellow.copy(alpha = 0.35f),
                            CoinbasePrimaryActive.copy(alpha = 0.08f),
                            CoinbaseCanvas
                        )
                    )
                )
        )

        QuickvntSparkLogo(
            modifier = Modifier
                .size(72.dp)
                .scale(logoScale * logoPulse)
                .alpha(logoAlpha)
        )
    }
}
