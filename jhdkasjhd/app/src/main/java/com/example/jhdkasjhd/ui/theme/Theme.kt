package com.example.jhdkasjhd.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CoinbaseLightColorScheme = lightColorScheme(
    primary = CoinbasePrimary,
    onPrimary = CoinbaseOnPrimary,
    primaryContainer = CoinbaseSurfaceStrong,
    onPrimaryContainer = CoinbaseInk,
    secondary = CoinbaseSurfaceStrong,
    onSecondary = CoinbaseInk,
    tertiary = CoinbaseAccentYellow,
    background = CoinbaseCanvas,
    onBackground = CoinbaseInk,
    surface = CoinbaseCanvas,
    onSurface = CoinbaseInk,
    surfaceVariant = CoinbaseSurfaceSoft,
    onSurfaceVariant = CoinbaseBody,
    outline = CoinbaseHairline,
    outlineVariant = CoinbaseHairlineSoft,
    error = CoinbaseSemanticDown,
    onError = CoinbaseOnPrimary
)

@Composable
fun JhdkasjhdTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CoinbaseCanvas.toArgb()
            window.navigationBarColor = CoinbaseCanvas.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = CoinbaseLightColorScheme,
        typography = CoinbaseTypography,
        shapes = CoinbaseShapes,
        content = content
    )
}
