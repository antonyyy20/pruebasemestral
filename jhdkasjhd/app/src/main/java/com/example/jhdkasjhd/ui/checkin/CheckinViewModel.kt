package com.example.jhdkasjhd.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.data.dto.CheckinRequest
import com.example.jhdkasjhd.data.dto.CheckinResponse
import com.example.jhdkasjhd.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckinUiState(
    val isLoading: Boolean = false,
    val lastCheckin: CheckinResponse? = null,
    val message: String? = null,
    val isError: Boolean = false
)

class CheckinViewModel(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckinUiState())
    val uiState: StateFlow<CheckinUiState> = _uiState.asStateFlow()

    fun validateQr(eventId: String, ticketId: String, qrSignature: String) {
        viewModelScope.launch {
            _uiState.value = CheckinUiState(isLoading = true)
            ticketRepository.validateCheckin(
                CheckinRequest(
                    ticketId = ticketId,
                    eventId = eventId,
                    qrSignature = qrSignature
                )
            ).onSuccess { checkin ->
                _uiState.value = CheckinUiState(
                    lastCheckin = checkin,
                    message = "Ingreso exitoso: ${checkin.checkinTime}"
                )
            }.onFailure {
                _uiState.value = CheckinUiState(
                    isError = true,
                    message = it.message ?: "Error al validar QR"
                )
            }
        }
    }

    fun showError(message: String) {
        _uiState.value = CheckinUiState(isError = true, message = message)
    }

    fun clearMessage() {
        _uiState.value = CheckinUiState()
    }
}
