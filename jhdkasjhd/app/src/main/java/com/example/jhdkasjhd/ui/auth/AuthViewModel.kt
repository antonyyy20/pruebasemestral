package com.example.jhdkasjhd.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.core.data.UserSession
import com.example.jhdkasjhd.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val profile: com.example.jhdkasjhd.data.dto.ProfileResponse? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val session: StateFlow<UserSession?> = authRepository.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    val userBio: StateFlow<String> = authRepository.userBioFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState(success = true) }
                .onFailure { _uiState.value = AuthUiState(error = it.message ?: "Error de login") }
        }
    }

    fun register(email: String, password: String, name: String, role: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.register(email, password, name, role)
                .onSuccess { _uiState.value = AuthUiState(success = true) }
                .onFailure { _uiState.value = AuthUiState(error = it.message ?: "Error de registro") }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _profileUiState.value = ProfileUiState()
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileUiState.value = _profileUiState.value.copy(isLoading = true, error = null)
            authRepository.getProfile()
                .onSuccess { profile ->
                    _profileUiState.value = _profileUiState.value.copy(
                        isLoading = false,
                        profile = profile
                    )
                }
                .onFailure {
                    _profileUiState.value = _profileUiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "No se pudo cargar el perfil"
                    )
                }
        }
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch {
            _profileUiState.value = _profileUiState.value.copy(
                isSaving = true,
                error = null,
                saved = false
            )
            authRepository.updateProfile(name.trim())
                .onSuccess { profile ->
                    authRepository.saveUserBio(bio.trim())
                    _profileUiState.value = _profileUiState.value.copy(
                        isSaving = false,
                        saved = true,
                        profile = profile
                    )
                }
                .onFailure {
                    _profileUiState.value = _profileUiState.value.copy(
                        isSaving = false,
                        error = it.message ?: "No se pudo guardar el perfil"
                    )
                }
        }
    }

    fun clearProfileSaved() {
        _profileUiState.value = _profileUiState.value.copy(saved = false)
    }
}
