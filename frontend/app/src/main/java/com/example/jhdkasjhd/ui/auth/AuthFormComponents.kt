package com.example.jhdkasjhd.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.R
import com.example.jhdkasjhd.ui.theme.CoinbaseAccentYellow
import com.example.jhdkasjhd.ui.theme.CoinbaseButtonTextStyle
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusMd
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticDown
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft

internal data class PasswordStrength(
    val label: String,
    val progress: Float,
    val color: androidx.compose.ui.graphics.Color
)

internal fun evaluatePasswordStrength(password: String): PasswordStrength? {
    if (password.isEmpty()) return null

    val hasLower = password.any { it.isLowerCase() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    val variety = listOf(hasLower, hasUpper, hasDigit, hasSymbol).count { it }

    return when {
        password.length < 6 -> PasswordStrength(
            label = "Débil",
            progress = 0.25f,
            color = CoinbaseSemanticDown
        )
        password.length < 8 || variety < 2 -> PasswordStrength(
            label = "Media",
            progress = 0.55f,
            color = CoinbaseAccentYellow
        )
        variety >= 3 && password.length >= 10 -> PasswordStrength(
            label = "Muy fuerte",
            progress = 1f,
            color = CoinbaseSemanticUp
        )
        else -> PasswordStrength(
            label = "Fuerte",
            progress = 0.82f,
            color = CoinbaseSemanticUp
        )
    }
}

@Composable
internal fun AuthFormScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    headerContent: @Composable (ColumnScope.() -> Unit)? = null,
    bottomContent: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CoinbaseSurfaceSoft)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CoinbaseSpacing.xs, vertical = CoinbaseSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = CoinbaseInk
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.weight(1f))

            if (onClose != null) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = CoinbaseInk
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CoinbaseSpacing.lg)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = CoinbaseInk,
                modifier = Modifier.fillMaxWidth()
            )

            if (headerContent != null) {
                Spacer(Modifier.height(CoinbaseSpacing.base))
                headerContent()
            }

            Spacer(Modifier.height(CoinbaseSpacing.lg))

            content()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CoinbaseSurfaceSoft)
                .padding(horizontal = CoinbaseSpacing.lg)
                .padding(top = CoinbaseSpacing.sm, bottom = CoinbaseSpacing.lg),
            content = bottomContent
        )
    }
}

@Composable
internal fun AuthLoginIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.login_corporate_illustration),
        contentDescription = "Ilustración de evento corporativo",
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(CoinbaseRadiusMd),
        contentScale = ContentScale.Fit
    )
}

@Composable
internal fun AuthGoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CoinbaseRadiusPill)
            .background(CoinbaseCanvas)
            .border(1.dp, CoinbaseInk, CoinbaseRadiusPill)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = CoinbaseSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null,
            tint = CoinbaseInk,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Continuar con Google",
            style = CoinbaseButtonTextStyle.copy(
                color = CoinbaseInk,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = CoinbaseSpacing.sm)
        )
    }
}

@Composable
internal fun AuthFormDivider(label: String = "o") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CoinbaseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(CoinbaseHairline)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CoinbaseMuted
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(CoinbaseHairline)
        )
    }
}

@Composable
internal fun AuthFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        error != null -> CoinbaseSemanticDown
        isFocused -> CoinbasePrimary
        else -> CoinbaseHairline
    }
    val borderWidth = if (isFocused || error != null) 2.dp else 1.dp
    val fieldShape = RoundedCornerShape(10.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(fieldShape)
                .background(CoinbaseCanvas)
                .border(borderWidth, borderColor, fieldShape)
                .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = CoinbaseMuted
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = CoinbaseInk,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = singleLine,
                    interactionSource = interactionSource,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = if (isPassword && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    }
                )
            }

            if (isPassword && onTogglePasswordVisibility != null) {
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = null,
                        tint = CoinbaseMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = CoinbaseSemanticDown,
                modifier = Modifier.padding(top = CoinbaseSpacing.xxs, start = 4.dp)
            )
        }
    }
}

@Composable
internal fun AuthPasswordStrengthBar(password: String) {
    val strength = evaluatePasswordStrength(password) ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = CoinbaseSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xxs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fortaleza de contraseña",
                style = MaterialTheme.typography.labelSmall,
                color = CoinbaseMuted
            )
            Text(
                text = strength.label,
                style = MaterialTheme.typography.labelSmall,
                color = strength.color,
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { strength.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CoinbaseRadiusMd),
            color = strength.color,
            trackColor = CoinbaseHairline
        )
    }
}

@Composable
internal fun AuthLegalNotice(modifier: Modifier = Modifier) {
    Text(
        text = "Al registrarte aceptas los Términos de servicio y la Política de privacidad de Quickvnt.",
        style = MaterialTheme.typography.bodySmall,
        color = CoinbaseMuted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}
