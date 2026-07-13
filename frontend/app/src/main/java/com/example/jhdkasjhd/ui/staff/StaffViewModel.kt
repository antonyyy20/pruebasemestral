package com.example.jhdkasjhd.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.StaffMemberResponse
import com.example.jhdkasjhd.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StaffEventsUiState(
    val isLoading: Boolean = false,
    val events: List<EventResponse> = emptyList(),
    val error: String? = null
)

data class StaffManagementUiState(
    val isLoading: Boolean = false,
    val members: List<StaffMemberResponse> = emptyList(),
    val operationSuccess: Boolean = false,
    val error: String? = null
)

class StaffEventsViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffEventsUiState())
    val uiState: StateFlow<StaffEventsUiState> = _uiState.asStateFlow()

    fun loadAssignedEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            eventRepository.listStaffEvents()
                .onSuccess { events ->
                    _uiState.value = StaffEventsUiState(events = events)
                }
                .onFailure {
                    _uiState.value = StaffEventsUiState(
                        error = it.message ?: "Error al cargar eventos asignados"
                    )
                }
        }
    }
}

class StaffManagementViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState.asStateFlow()

    fun loadStaff(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            eventRepository.listEventStaff(eventId)
                .onSuccess { members ->
                    _uiState.value = StaffManagementUiState(members = members)
                }
                .onFailure {
                    _uiState.value = StaffManagementUiState(
                        error = it.message ?: "Error al cargar el staff del evento"
                    )
                }
        }
    }

    fun createStaff(eventId: String, name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            val result = createStaffWithOptionalRetry(eventId, email.trim(), password, name.trim())
            result
                .onSuccess {
                    eventRepository.listEventStaff(eventId)
                        .onSuccess { members ->
                            _uiState.value = StaffManagementUiState(
                                members = members,
                                operationSuccess = true
                            )
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message ?: "Staff creado pero no se pudo recargar la lista"
                            )
                        }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "No se pudo crear la cuenta de staff"
                    )
                }
        }
    }

    private suspend fun createStaffWithOptionalRetry(
        eventId: String,
        email: String,
        password: String,
        name: String
    ): Result<StaffMemberResponse> {
        val firstAttempt = eventRepository.createStaff(eventId, email, password, name)
        if (firstAttempt.isSuccess) return firstAttempt

        val message = firstAttempt.exceptionOrNull()?.message.orEmpty()
        val shouldRetry = message.contains("500", ignoreCase = true) ||
            message.contains("no está disponible", ignoreCase = true) ||
            message.contains("interno", ignoreCase = true)

        return if (shouldRetry) {
            eventRepository.createStaff(eventId, email, password, name)
        } else {
            firstAttempt
        }
    }

    fun removeStaff(eventId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            eventRepository.removeStaff(eventId, userId)
                .onSuccess { loadStaff(eventId) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "No se pudo eliminar al miembro del staff"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, operationSuccess = false)
    }
}
