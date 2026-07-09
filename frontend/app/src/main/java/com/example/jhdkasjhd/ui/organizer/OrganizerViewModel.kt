package com.example.jhdkasjhd.ui.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.data.dto.EventCreateRequest
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.EventUpdateRequest
import com.example.jhdkasjhd.data.repository.AuthRepository
import com.example.jhdkasjhd.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class OrganizerUiState(
    val isLoading: Boolean = false,
    val events: List<EventResponse> = emptyList(),
    val selectedEvent: EventResponse? = null,
    val operationSuccess: Boolean = false,
    val error: String? = null
)

class OrganizerViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrganizerUiState())
    val uiState: StateFlow<OrganizerUiState> = _uiState.asStateFlow()

    fun loadMyEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val session = authRepository.sessionFlow.first()
            val organizerId = session?.userId ?: return@launch

            eventRepository.listAllOrganizerEvents(organizerId)
                .onSuccess { events ->
                    _uiState.value = OrganizerUiState(events = events)
                }
                .onFailure {
                    _uiState.value = OrganizerUiState(error = it.message ?: "Error al cargar eventos")
                }
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            eventRepository.getEvent(eventId)
                .onSuccess { event ->
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedEvent = event)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error al cargar evento"
                    )
                }
        }
    }

    fun createEvent(request: EventCreateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            eventRepository.createEvent(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true)
                    loadMyEvents()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error al crear evento"
                    )
                }
        }
    }

    fun updateEvent(eventId: String, request: EventUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            eventRepository.updateEvent(eventId, request)
                .onSuccess { event ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedEvent = event,
                        operationSuccess = true
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error al actualizar evento"
                    )
                }
        }
    }

    fun publishEvent(eventId: String) {
        viewModelScope.launch {
            eventRepository.publishEvent(eventId)
                .onSuccess { loadMyEvents() }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message ?: "Error al publicar")
                }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId)
                .onSuccess { loadMyEvents() }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message ?: "Error al eliminar")
                }
        }
    }

    fun clearOperationSuccess() {
        _uiState.value = _uiState.value.copy(operationSuccess = false)
    }
}
