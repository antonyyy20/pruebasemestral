package com.example.jhdkasjhd.ui.marketplace

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft

@Composable
fun MarketplaceScreen(
    onEventClick: (String) -> Unit,
    onSeeAllCategories: () -> Unit,
    onCategoryClick: (String) -> Unit,
    initialCategory: String? = null,
    viewModel: MarketplaceViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            viewModel.selectCategory(initialCategory)
        }
    }

    when {
        uiState.isLoading && uiState.events.isEmpty() -> LoadingBox(
            modifier = Modifier
                .fillMaxSize()
                .background(CoinbaseCanvas)
        )
        uiState.error != null && uiState.events.isEmpty() -> ErrorMessage(
            message = uiState.error!!,
            modifier = Modifier
                .fillMaxSize()
                .background(CoinbaseCanvas)
                .padding(CoinbaseSpacing.base),
            onRetry = { viewModel.loadEvents(initialCategory) }
        )
        else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CoinbaseCanvas),
            contentPadding = PaddingValues(
                start = CoinbaseSpacing.base,
                end = CoinbaseSpacing.base,
                top = CoinbaseSpacing.base,
                bottom = CoinbaseSpacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xl)
        ) {
            item {
                EventbriteDiscoverSearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchChange = viewModel::updateSearchQuery,
                    locationLabel = "Panamá",
                    onFilterClick = onSeeAllCategories
                )
            }

            if (uiState.selectedCategory != null) {
                item {
                    ActiveFilterChip(
                        label = uiState.selectedCategory!!,
                        onClear = viewModel::clearCategoryFilter
                    )
                }
            }

            if (uiState.discoverEvents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = CoinbaseSpacing.xl),
                        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
                    ) {
                        Text(
                            text = "No encontramos eventos",
                            style = MaterialTheme.typography.titleMedium,
                            color = CoinbaseInk
                        )
                        Text(
                            text = "Prueba con otra búsqueda o explora las categorías.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoinbaseMuted
                        )
                    }
                }
            } else {
                items(uiState.discoverEvents, key = { it.id }) { event ->
                    DiscoverEventCard(
                        event = event,
                        isSaved = event.id in uiState.savedEventIds,
                        onToggleSave = { viewModel.toggleSaved(event.id) },
                        onShare = {
                            val shareText = buildString {
                                append(event.title)
                                append("\n")
                                append(formatEventDiscoverMeta(event))
                                append("\nDesde $0")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir evento"))
                        },
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit,
    viewModel: MarketplaceViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    QuickvntScaffold(title = "Categorías") { padding ->
        when {
            uiState.isLoading && uiState.categories.isEmpty() -> LoadingBox(Modifier.padding(padding))
            uiState.error != null && uiState.categories.isEmpty() -> ErrorMessage(
                message = uiState.error!!,
                modifier = Modifier.padding(padding),
                onRetry = { viewModel.loadEvents() }
            )
            uiState.categories.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(CoinbaseSpacing.base)
            ) {
                CoinbaseFeatureCard {
                    Text(
                        text = "Aún no hay categorías disponibles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoinbaseMuted
                    )
                }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(CoinbaseSpacing.base),
                horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
            ) {
                items(uiState.categories, key = { it.name }) { category ->
                    CategoryGridCard(
                        category = category,
                        onClick = { onCategoryClick(category.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(
    eventId: String,
    isOrganizer: Boolean,
    onRegisterClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onScanClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: MarketplaceViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(eventId) {
        viewModel.loadEventDetail(eventId)
    }

    val event = uiState.selectedEvent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoinbaseSurfaceSoft)
    ) {
        when {
            uiState.isLoading && event == null -> LoadingBox(Modifier.fillMaxSize())
            uiState.error != null && event == null -> ErrorMessage(
                message = uiState.error!!,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CoinbaseSpacing.base),
                onRetry = { viewModel.loadEventDetail(eventId) }
            )
            event != null -> Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(CoinbaseSurfaceSoft),
                    contentPadding = PaddingValues(
                        top = CoinbaseSpacing.base,
                        bottom = CoinbaseSpacing.base
                    )
                ) {
                    item {
                        EventDetailContent(
                            event = event,
                            isOrganizer = isOrganizer,
                            isSaved = event.id in uiState.savedEventIds,
                            onBack = onBack,
                            onShare = {
                                val shareText = buildString {
                                    append(event.title)
                                    append("\n")
                                    append(formatEventLocationLine(event))
                                    append("\n")
                                    append(formatEventDetailDateTime(event.dateStart))
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir evento"))
                            },
                            onToggleSave = { viewModel.toggleSaved(event.id) },
                            onRegisterClick = onRegisterClick,
                            onAnalyticsClick = onAnalyticsClick,
                            onScanClick = onScanClick
                        )
                    }
                }

                if (isOrganizer) {
                    EventDetailOrganizerBottomBar(
                        event = event,
                        onAnalyticsClick = onAnalyticsClick
                    )
                } else {
                    EventDetailAttendeeBottomBar(
                        event = event,
                        onRegisterClick = onRegisterClick,
                        enabled = event.status == "PUBLISHED"
                    )
                }
            }
        }
    }
}
