package com.example.jhdkasjhd.ui.auth

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.jhdkasjhd.ui.components.CoinbaseInlineMessage
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseTertiaryButton
import com.example.jhdkasjhd.ui.theme.CoinbaseBody
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
    onLoginSuccess: (role: String) -> Unit,
    viewModel: AuthViewModel = quickvntViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showValidationErrors by rememberSaveable { mutableStateOf(false) }
    var googleInfoMessage by rememberSaveable { mutableStateOf<String?>(null) }
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
            onLoginSuccess(session!!.role)
        }
    }

    AuthFormScaffold(
        title = "Iniciar sesión",
        headerContent = {
            AuthLoginIllustration()
            Spacer(Modifier.height(CoinbaseSpacing.base))
            AuthGoogleSignInButton(
                onClick = {
                    googleInfoMessage =
                        "Inicio con Google estará disponible pronto. Usa tu correo por ahora."
                    if (uiState.error != null) viewModel.clearError()
                },
                enabled = !uiState.isLoading
            )
            googleInfoMessage?.let { message ->
                Spacer(Modifier.height(CoinbaseSpacing.sm))
                CoinbaseInlineMessage(message = message, isError = false)
            }
            AuthFormDivider(label = "o")
        },
        bottomContent = {
            uiState.error?.let { message ->
                CoinbaseInlineMessage(message = message, isError = true)
                Spacer(Modifier.height(CoinbaseSpacing.sm))
            }

            CoinbasePrimaryButton(
                text = "Iniciar sesión",
                onClick = { attemptLogin() },
                loading = uiState.isLoading,
                enabled = !uiState.isLoading,
                large = true
            )

            Spacer(Modifier.height(CoinbaseSpacing.base))

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
    ) {
        AuthFormTextField(
            value = email,
            onValueChange = {
                email = it
                googleInfoMessage = null
                if (uiState.error != null) viewModel.clearError()
            },
            label = "Correo electrónico",
            error = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))

        AuthFormTextField(
            value = password,
            onValueChange = {
                password = it
                googleInfoMessage = null
                if (uiState.error != null) viewModel.clearError()
            },
            label = "Contraseña",
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
    }
}

@Composable
fun RegisterScreen(
    onNavigateLogin: () -> Unit,
    onRegisterSuccess: (role: String) -> Unit,
    viewModel: AuthViewModel = quickvntViewModel()
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var role by rememberSaveable { mutableStateOf("ATTENDEE") }
    var showValidationErrors by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val session by viewModel.session.collectAsState()

    val trimmedEmail = email.trim()
    val trimmedConfirmEmail = confirmEmail.trim()

    val firstNameError = if (showValidationErrors && firstName.isBlank()) {
        "Ingresa tu nombre"
    } else null
    val lastNameError = if (showValidationErrors && lastName.isBlank()) {
        "Ingresa tu apellido"
    } else null
    val emailError = when {
        !showValidationErrors -> null
        trimmedEmail.isBlank() -> "Ingresa tu correo electrónico"
        !isValidEmail(trimmedEmail) -> "Ingresa un correo electrónico válido"
        else -> null
    }
    val confirmEmailError = when {
        !showValidationErrors -> null
        trimmedConfirmEmail.isBlank() -> "Confirma tu correo electrónico"
        trimmedConfirmEmail != trimmedEmail -> "Los correos no coinciden"
        else -> null
    }
    val passwordError = when {
        !showValidationErrors -> null
        password.isBlank() -> "Ingresa tu contraseña"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    val isFormValid = firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        isValidEmail(trimmedEmail) &&
        trimmedConfirmEmail == trimmedEmail &&
        password.length >= 6

    fun attemptRegister() {
        showValidationErrors = true
        if (!isFormValid || uiState.isLoading) return
        val fullName = "${firstName.trim()} ${lastName.trim()}".trim()
        viewModel.register(trimmedEmail, password, fullName, role.uppercase())
    }

    LaunchedEffect(uiState.success, session) {
        if (uiState.success && session != null) {
            onRegisterSuccess(session!!.role)
        }
    }

    AuthFormScaffold(
        title = "Crear una cuenta",
        onBack = onNavigateLogin,
        onClose = onNavigateLogin,
        bottomContent = {
            AuthLegalNotice()
            Spacer(Modifier.height(CoinbaseSpacing.base))

            uiState.error?.let { message ->
                CoinbaseInlineMessage(message = message, isError = true)
                Spacer(Modifier.height(CoinbaseSpacing.sm))
            }

            CoinbasePrimaryButton(
                text = "Registrarse",
                onClick = { attemptRegister() },
                loading = uiState.isLoading,
                enabled = !uiState.isLoading,
                large = true
            )

            Spacer(Modifier.height(CoinbaseSpacing.sm))

            CoinbaseTertiaryButton(
                text = "¿Ya tienes cuenta? Inicia sesión",
                onClick = onNavigateLogin,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        AuthFormTextField(
            value = email,
            onValueChange = {
                email = it
                if (uiState.error != null) viewModel.clearError()
            },
            label = "Correo electrónico",
            error = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))

        AuthFormTextField(
            value = confirmEmail,
            onValueChange = {
                confirmEmail = it
                if (uiState.error != null) viewModel.clearError()
            },
            label = "Confirmar correo electrónico",
            error = confirmEmailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))

        AuthFormTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = "Nombre",
            error = firstNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))

        AuthFormTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = "Apellido",
            error = lastNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(CoinbaseSpacing.sm))

        Column {
            AuthFormTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (uiState.error != null) viewModel.clearError()
                },
                label = "Contraseña",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                error = passwordError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { attemptRegister() })
            )
            AuthPasswordStrengthBar(password = password)
        }

        Spacer(modifier = Modifier.height(CoinbaseSpacing.base))

        Text(
            text = "Tipo de cuenta",
            style = MaterialTheme.typography.labelMedium,
            color = CoinbaseMuted
        )
        Spacer(Modifier.height(CoinbaseSpacing.xs))
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
    }
}
