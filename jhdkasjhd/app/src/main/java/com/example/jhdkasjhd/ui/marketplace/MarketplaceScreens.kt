package com.example.jhdkasjhd.ui.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.core.util.StatusLabels
import com.example.jhdkasjhd.ui.auth.AuthViewModel
import com.example.jhdkasjhd.ui.components.CoinbaseDetailRow
import com.example.jhdkasjhd.ui.components.CoinbaseFeatureCard
import com.example.jhdkasjhd.ui.components.CoinbasePrimaryButton
import com.example.jhdkasjhd.ui.components.CoinbaseSecondaryButton
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.components.QuickvntScaffold
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusXl
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun MarketplaceScreen(
    onEventClick: (String) -> Unit,
    onSeeAllCategories: () -> Unit,
    onCategoryClick: (String) -> Unit,
    initialCategory: String? = null,
    viewModel: MarketplaceViewModel = quickvntViewModel(),
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by authViewModel.session.collectAsState()

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            viewModel.selectCategory(initialCategory)
        }
    }

    when {
        uiState.isLoading && uiState.events.isEmpty() -> LoadingBox()
        uiState.error != null && uiState.events.isEmpty() -> ErrorMessage(
            message = uiState.error!!,
            onRetry = { viewModel.loadEvents(initialCategory) }
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = CoinbaseSpacing.base,
                end = CoinbaseSpacing.base,
                top = CoinbaseSpacing.base,
                bottom = CoinbaseSpacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.lg)
        ) {
            item {
                DiscoverHeader(
                    userName = session?.name,
                    locationLabel = "Explorar eventos",
                    searchQuery = uiState.searchQuery,
                    onSearchChange = viewModel::updateSearchQuery
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

            if (uiState.categories.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Categorías",
                        actionLabel = "Ver todas",
                        onActionClick = onSeeAllCategories
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
                        items(uiState.categories.take(6), key = { it.name }) { category ->
                            CategoryPreviewCard(
                                category = category,
                                onClick = { onCategoryClick(category.name) }
                            )
                        }
                    }
                }
            }

            if (uiState.featuredEvents.isNotEmpty()) {
                item {
                    SectionHeader(title = "Próximos eventos")
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)) {
                        items(uiState.featuredEvents, key = { it.id }) { event ->
                            FeaturedEventCard(
                                event = event,
                                onClick = { onEventClick(event.id) }
                            )
                        }
                    }
                }
            }

            item {
                DiscoverDivider()
            }

            item {
                SectionHeader(title = "Eventos cerca de ti")
            }

            if (uiState.nearbyEvents.isEmpty()) {
                item {
                    CoinbaseFeatureCard {
                        Text(
                            text = "No encontramos eventos con esos filtros.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoinbaseMuted
                        )
                    }
                }
            } else {
                items(uiState.nearbyEvents, key = { it.id }) { event ->
                    NearbyEventRow(
                        event = event,
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

    LaunchedEffect(eventId) {
        viewModel.loadEventDetail(eventId)
    }

    val event = uiState.selectedEvent

    QuickvntScaffold(title = event?.title ?: "Detalle del evento", onBack = onBack) { padding ->
        when {
            uiState.isLoading && event == null -> LoadingBox(Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, Modifier.padding(padding)) {
                viewModel.loadEventDetail(eventId)
            }
            event != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = CoinbaseSpacing.base)
                        .clip(CoinbaseRadiusXl)
                ) {
                    EventImage(
                        bannerUrl = event.bannerUrl,
                        category = event.category,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = CoinbaseSpacing.base),
                    verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
                ) {
                CoinbaseFeatureCard {
                    Text(event.description, style = MaterialTheme.typography.bodyLarge, color = CoinbaseInk)
                }

                CoinbaseFeatureCard {
                    Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)) {
                        CoinbaseDetailRow("Ubicación", event.location)
                        CoinbaseDetailRow("Categoría", event.category)
                        CoinbaseDetailRow("Inicio", event.dateStart)
                        CoinbaseDetailRow("Fin", event.dateEnd)
                        CoinbaseDetailRow("Capacidad", event.capacity.toString())
                        CoinbaseDetailRow("Estado", StatusLabels.eventStatus(event.status))
                    }
                }

                if (!isOrganizer && event.status == "PUBLISHED") {
                    CoinbasePrimaryButton(
                        text = "Registrarme al evento",
                        onClick = onRegisterClick
                    )
                }

                if (isOrganizer) {
                    CoinbasePrimaryButton(text = "Ver analíticas", onClick = onAnalyticsClick)
                    CoinbaseSecondaryButton(
                        text = "Escanear QR de ingreso",
                        onClick = onScanClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                }
            }
        }
    }
}
