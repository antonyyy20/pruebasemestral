package com.example.jhdkasjhd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.ui.theme.CoinbaseBody
import com.example.jhdkasjhd.ui.theme.CoinbaseButtonTextStyle
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseNumberStyle
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimaryActive
import com.example.jhdkasjhd.ui.theme.CoinbasePrimaryDisabled
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusMd
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusXl
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticDown
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceStrong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickvntScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = CoinbaseCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CoinbaseInk
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = CoinbaseInk
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CoinbaseCanvas,
                    titleContentColor = CoinbaseInk
                )
            )
        },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar
    ) { padding ->
        content(padding)
    }
}

data class CoinbaseNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isSelected: (currentRoute: String?) -> Boolean
)

@Composable
fun CoinbaseBottomNavigationBar(
    items: List<CoinbaseNavItem>,
    currentRoute: String?,
    onItemClick: (CoinbaseNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CoinbaseCanvas)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = CoinbaseHairline
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.isSelected(currentRoute)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemClick(item) }
                        )
                        .semantics {
                            contentDescription = item.label
                            role = Role.Tab
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (selected) CoinbasePrimary else CoinbaseInk,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CoinbasePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    large: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(if (large) 56.dp else 44.dp),
        enabled = enabled && !loading,
        shape = CoinbaseRadiusPill,
        colors = ButtonDefaults.buttonColors(
            containerColor = CoinbasePrimary,
            contentColor = CoinbaseOnPrimary,
            disabledContainerColor = CoinbasePrimaryDisabled,
            disabledContentColor = CoinbaseOnPrimary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = CoinbaseOnPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = CoinbaseButtonTextStyle)
        }
    }
}

@Composable
fun CoinbaseSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = CoinbaseRadiusPill,
        colors = ButtonDefaults.buttonColors(
            containerColor = CoinbaseSurfaceStrong,
            contentColor = CoinbaseInk,
            disabledContainerColor = CoinbaseSurfaceStrong.copy(alpha = 0.6f),
            disabledContentColor = CoinbaseMuted
        )
    ) {
        Text(text = text, style = CoinbaseButtonTextStyle)
    }
}

@Composable
fun CoinbaseOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = CoinbaseRadiusPill,
        border = androidx.compose.foundation.BorderStroke(1.dp, CoinbaseHairline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = CoinbaseInk
        )
    ) {
        Text(text = text, style = CoinbaseButtonTextStyle)
    }
}

@Composable
fun CoinbaseTertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = CoinbaseButtonTextStyle,
            color = CoinbasePrimary
        )
    }
}

@Composable
fun QuickvntTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    placeholder: String = "",
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

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CoinbaseMuted,
            modifier = Modifier.padding(bottom = CoinbaseSpacing.xs)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(CoinbaseRadiusMd)
                .background(CoinbaseCanvas)
                .border(borderWidth, borderColor, CoinbaseRadiusMd)
                .padding(horizontal = CoinbaseSpacing.base),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = CoinbaseInk, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                singleLine = singleLine,
                interactionSource = interactionSource,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CoinbaseMuted.copy(alpha = 0.7f)
                            )
                        }
                        inner()
                    }
                }
            )
            if (isPassword && onTogglePasswordVisibility != null) {
                IconButton(onClick = onTogglePasswordVisibility, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
fun CoinbaseFeatureCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(CoinbaseRadiusXl)
        .background(CoinbaseCanvas)
        .border(1.dp, CoinbaseHairline, CoinbaseRadiusXl)
        .padding(CoinbaseSpacing.base)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Column(modifier = cardModifier, content = { content() })
}

@Composable
fun CoinbaseBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = CoinbaseInk,
        modifier = modifier
            .clip(CoinbaseRadiusPill)
            .background(CoinbaseSurfaceStrong)
            .padding(horizontal = CoinbaseSpacing.sm, vertical = CoinbaseSpacing.xxs)
    )
}

@Composable
fun CoinbaseDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = CoinbaseInk)
    }
}

@Composable
fun CoinbaseKpiCard(label: String, value: String, semanticPositive: Boolean? = null) {
    CoinbaseFeatureCard {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = CoinbaseMuted)
        Text(
            text = value,
            style = CoinbaseNumberStyle.copy(
                color = when (semanticPositive) {
                    true -> CoinbaseSemanticUp
                    false -> CoinbaseSemanticDown
                    null -> CoinbaseInk
                }
            )
        )
    }
}

@Composable
fun CoinbaseProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CoinbaseRadiusPill),
        color = CoinbasePrimary,
        trackColor = CoinbaseSurfaceStrong
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CoinbasePrimary)
    }
}

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CoinbaseSpacing.base),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = CoinbaseSemanticDown,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            CoinbaseSecondaryButton(text = "Reintentar", onClick = onRetry)
        }
    }
}

@Composable
fun CoinbaseEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(CoinbaseSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = CoinbaseBody,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            CoinbasePrimaryButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.padding(top = CoinbaseSpacing.lg)
            )
        }
    }
}

@Composable
fun CoinbaseSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = CoinbaseInk,
        modifier = modifier.padding(bottom = CoinbaseSpacing.xs)
    )
}

@Composable
fun CoinbaseInlineMessage(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) CoinbaseSemanticDown else CoinbaseSemanticUp,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}
