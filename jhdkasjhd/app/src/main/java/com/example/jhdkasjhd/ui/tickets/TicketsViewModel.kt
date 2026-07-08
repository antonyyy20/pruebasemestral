package com.example.jhdkasjhd.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.dto.TicketResponse
import com.example.jhdkasjhd.data.repository.EventRepository
import com.example.jhdkasjhd.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketsUiState(
    val isLoading: Boolean = false,
    val tickets: List<TicketResponse> = emptyList(),
    val selectedTicket: TicketResponse? = null,
    val eventForRegistration: EventResponse? = null,
    val registrationSuccess: Boolean = false,
    val error: String? = null
)

class TicketsViewModel(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    fun loadMyTickets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            ticketRepository.myTickets()
                .onSuccess { tickets ->
                    _uiState.value = TicketsUiState(tickets = tickets)
                }
                .onFailure {
                    _uiState.value = TicketsUiState(error = it.message ?: "Error al cargar boletos")
                }
        }
    }

    fun loadTicket(ticketId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            ticketRepository.getTicket(ticketId)
                .onSuccess { ticket ->
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedTicket = ticket)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error al cargar ticket"
                    )
                }
        }
    }

    fun loadEventForRegistration(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            eventRepository.getEvent(eventId)
                .onSuccess { event ->
                    _uiState.value = _uiState.value.copy(isLoading = false, eventForRegistration = event)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error al cargar evento"
                    )
                }
        }
    }

    fun registerToEvent(eventId: String, formResponse: Map<String, Any?>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, registrationSuccess = false)
            ticketRepository.registerToEvent(eventId, formResponse)
                .onSuccess { ticket ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedTicket = ticket,
                        registrationSuccess = true
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error al registrarse"
                    )
                }
        }
    }
}
