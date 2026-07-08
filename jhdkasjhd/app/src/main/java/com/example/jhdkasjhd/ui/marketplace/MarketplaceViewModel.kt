package com.example.jhdkasjhd.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val events: List<EventResponse> = emptyList(),
    val error: String? = null,
    val selectedEvent: EventResponse? = null
)

class MarketplaceViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents(category: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            eventRepository.listPublishedEvents(category)
                .onSuccess { events ->
                    _uiState.value = MarketplaceUiState(events = events)
                }
                .onFailure {
                    _uiState.value = MarketplaceUiState(error = it.message ?: "Error al cargar eventos")
                }
        }
    }

    fun loadEventDetail(eventId: String) {
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
}
