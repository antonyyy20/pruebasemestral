package com.example.jhdkasjhd.ui.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbaseInlineMessage
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseTertiaryButton
import com.example.jhdkasjhd.ui.components.QuickvntTextField
import com.example.jhdkasjhd.ui.theme.CoinbaseBody
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceStrong

private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
}

@Composable
fun LoginScreen(
    onNavigateRegister: () -> Unit,
    onLoginSuccess: (isOrganizer: Boolean) -> Unit,
    viewModel: AuthViewModel = quickvntViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showValidationErrors by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val session by viewModel.session.collectAsState()
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    val trimmedEmail = email.trim()
    val emailError = when {
        !showValidationErrors -> null
        trimmedEmail.isBlank() -> "Ingresa tu correo electrónico"
        !isValidEmail(trimmedEmail) -> "Ingresa un correo electrónico válido"
        else -> null
    }
    val passwordError = when {
        !showValidationErrors -> null
        password.isBlank() -> "Ingresa tu contraseña"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }
    val isFormValid = trimmedEmail.isNotBlank() &&
        password.isNotBlank() &&
        isValidEmail(trimmedEmail) &&
        password.length >= 6

    fun attemptLogin() {
        showValidationErrors = true
        if (!isFormValid || uiState.isLoading) return
        focusManager.clearFocus()
        viewModel.login(trimmedEmail, password)
    }

    LaunchedEffect(uiState.success, session) {
        if (uiState.success && session != null) {
            onLoginSuccess(session!!.isOrganizer)
        }
    }

    AuthLightLayout(
        headline = "¡Hola!",
        subheadline = "Bienvenido de nuevo."
    ) {
        QuickvntTextField(
            value = email,
            onValueChange = {
                email = it
                if (uiState.error != null) viewModel.clearError()
            },
            label = "Correo electrónico",
            placeholder = "juan@ejemplo.com",
            error = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.base))

        QuickvntTextField(
            value = password,
            onValueChange = {
                password = it
                if (uiState.error != null) viewModel.clearError()
            },
            label = "Contraseña",
            placeholder = "Ingresa tu contraseña",
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            error = passwordError,
            modifier = Modifier.focusRequester(passwordFocusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { attemptLogin() })
        )

        CoinbaseTertiaryButton(
            text = "¿Olvidaste tu contraseña?",
            onClick = { },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = CoinbaseSpacing.xs)
        )

        uiState.error?.let { message ->
            Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))
            CoinbaseInlineMessage(message = message, isError = true)
        }

        Spacer(modifier = Modifier.height(CoinbaseSpacing.lg))

        CoinbasePrimaryButton(
            text = "Iniciar sesión",
            onClick = { attemptLogin() },
            loading = uiState.isLoading,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.lg))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = CoinbaseBody)) {
                    append("¿No tienes cuenta? ")
                }
                withStyle(SpanStyle(color = CoinbasePrimary)) {
                    append("Regístrate")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateRegister)
                .padding(vertical = CoinbaseSpacing.xs)
        )
    }
}

@Composable
fun RegisterScreen(
    onNavigateLogin: () -> Unit,
    onRegisterSuccess: (isOrganizer: Boolean) -> Unit,
    viewModel: AuthViewModel = quickvntViewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var role by rememberSaveable { mutableStateOf("ATTENDEE") }
    val uiState by viewModel.uiState.collectAsState()
    val session by viewModel.session.collectAsState()

    LaunchedEffect(uiState.success, session) {
        if (uiState.success && session != null) {
            onRegisterSuccess(session!!.isOrganizer)
        }
    }

    AuthLightLayout(
        headline = "Únete a Quickvnt",
        subheadline = "Crea tu cuenta para comenzar."
    ) {
        QuickvntTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre completo",
            placeholder = "Juan Pérez"
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.base))

        QuickvntTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo electrónico",
            placeholder = "juan@ejemplo.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.base))

        QuickvntTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            placeholder = "Al menos 6 caracteres",
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))

        Text(
            text = "Tipo de cuenta",
            style = MaterialTheme.typography.labelMedium,
            color = CoinbaseMuted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
            FilterChip(
                selected = role == "ATTENDEE",
                onClick = { role = "ATTENDEE" },
                label = { Text("Asistente") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CoinbasePrimary,
                    selectedLabelColor = CoinbaseOnPrimary,
                    containerColor = CoinbaseSurfaceStrong,
                    labelColor = CoinbaseInk
                )
            )
            FilterChip(
                selected = role == "ORGANIZER",
                onClick = { role = "ORGANIZER" },
                label = { Text("Organizador") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CoinbasePrimary,
                    selectedLabelColor = CoinbaseOnPrimary,
                    containerColor = CoinbaseSurfaceStrong,
                    labelColor = CoinbaseInk
                )
            )
        }

        uiState.error?.let { message ->
            Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))
            CoinbaseInlineMessage(message = message, isError = true)
        }

        Spacer(modifier = Modifier.height(CoinbaseSpacing.lg))

        CoinbasePrimaryButton(
            text = "Registrarse",
            onClick = { viewModel.register(email.trim(), password, name.trim(), role.uppercase()) },
            loading = uiState.isLoading,
            enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.lg))

        CoinbaseTertiaryButton(
            text = "¿Ya tienes cuenta? Inicia sesión",
            onClick = onNavigateLogin,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AuthLightLayout(
    headline: String,
    subheadline: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoinbaseCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CoinbaseSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(CoinbaseSpacing.xxl))

            Text(
                text = headline,
                style = MaterialTheme.typography.displaySmall,
                color = CoinbaseInk,
                textAlign = TextAlign.Center
            )
            Text(
                text = subheadline,
                style = MaterialTheme.typography.bodyLarge,
                color = CoinbaseBody,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = CoinbaseSpacing.xs)
            )

            Spacer(modifier = Modifier.height(CoinbaseSpacing.xl))

            CoinbaseFeatureCard {
                content()
            }

            Spacer(modifier = Modifier.height(CoinbaseSpacing.xl))
        }
    }
}
