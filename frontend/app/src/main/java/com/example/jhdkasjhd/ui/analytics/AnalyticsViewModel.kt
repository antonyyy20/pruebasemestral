package com.example.jhdkasjhd.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.data.dto.AnalyticsResponse
import com.example.jhdkasjhd.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val analytics: AnalyticsResponse? = null,
    val error: String? = null
)

class AnalyticsViewModel(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadAnalytics(eventId: String) {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState(isLoading = true)
            ticketRepository.getAnalytics(eventId)
                .onSuccess { analytics ->
                    _uiState.value = AnalyticsUiState(analytics = analytics)
                }
                .onFailure {
                    _uiState.value = AnalyticsUiState(error = it.message ?: "Error al cargar analíticas")
                }
        }
    }
}
