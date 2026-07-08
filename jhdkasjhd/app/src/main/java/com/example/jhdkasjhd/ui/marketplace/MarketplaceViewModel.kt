package com.example.jhdkasjhd.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryItem(
    val name: String,
    val eventCount: Int
)

data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val events: List<EventResponse> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val savedEventIds: Set<String> = emptySet(),
    val error: String? = null,
    val selectedEvent: EventResponse? = null
) {
    val categories: List<CategoryItem>
        get() = events
            .groupBy { it.category.ifBlank { "General" } }
            .map { (name, items) -> CategoryItem(name = name, eventCount = items.size) }
            .sortedBy { it.name }

    private val visibleEvents: List<EventResponse>
        get() {
            val query = searchQuery.trim()
            return events.filter { event ->
                val matchesCategory = selectedCategory == null || event.category == selectedCategory
                val matchesSearch = query.isBlank() ||
                    event.title.contains(query, ignoreCase = true) ||
                    event.location.contains(query, ignoreCase = true) ||
                    event.category.contains(query, ignoreCase = true) ||
                    event.description.contains(query, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }

    val discoverEvents: List<EventResponse>
        get() = visibleEvents.sortedBy { it.dateStart }

    val featuredEvents: List<EventResponse>
        get() = visibleEvents
            .sortedBy { it.dateStart }
            .take(6)

    val nearbyEvents: List<EventResponse>
        get() = visibleEvents
            .sortedByDescending { it.createdAt.orEmpty() }
}

class MarketplaceViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents(category: String? = _uiState.value.selectedCategory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedCategory = category
            )
            eventRepository.listPublishedEvents(category)
                .onSuccess { events ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        events = events,
                        selectedCategory = category
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error al cargar eventos"
                    )
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun clearCategoryFilter() {
        if (_uiState.value.selectedCategory != null) {
            loadEvents(category = null)
        } else {
            _uiState.value = _uiState.value.copy(selectedCategory = null)
        }
    }

    fun selectCategory(category: String) {
        loadEvents(category = category)
    }

    fun toggleSaved(eventId: String) {
        val current = _uiState.value.savedEventIds
        _uiState.value = _uiState.value.copy(
            savedEventIds = if (eventId in current) current - eventId else current + eventId
        )
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
