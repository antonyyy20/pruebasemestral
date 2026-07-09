package com.example.jhdkasjhd.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.jhdkasjhd.core.quickvntViewModel
import com.example.jhdkasjhd.ui.components.ErrorMessage
import com.example.jhdkasjhd.ui.components.LoadingBox
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing

@Composable
fun AnalyticsScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = quickvntViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var viewModeName by rememberSaveable { mutableStateOf(AnalyticsViewMode.SUMMARY.name) }
    val viewMode = AnalyticsViewMode.entries.firstOrNull { it.name == viewModeName }
        ?: AnalyticsViewMode.SUMMARY
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(eventId) { viewModel.loadAnalytics(eventId) }

    if (showInfoDialog) {
        AnalyticsInfoDialog(onDismiss = { showInfoDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoinbaseCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnalyticsTopBar(
            onBack = onBack,
            onInfoClick = { showInfoDialog = true }
        )

        when {
            uiState.isLoading -> LoadingBox()
            uiState.error != null -> ErrorMessage(
                message = uiState.error!!,
                modifier = Modifier.padding(CoinbaseSpacing.base),
                onRetry = { viewModel.loadAnalytics(eventId) }
            )
            uiState.analytics != null -> {
                val data = uiState.analytics!!
                val chartModel = buildAnalyticsChartModel(data, viewMode)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(PaddingValues(CoinbaseSpacing.base)),
                    verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.lg)
                ) {
                    AnalyticsFilterRow(
                        selectedMode = viewMode,
                        onModeSelected = { viewModeName = it.name }
                    )

                    AnalyticsHeroMetrics(
                        data = data,
                        chartLabel = "Por cupos"
                    )

                    AnalyticsBarChartSection(model = chartModel)

                    AnalyticsLegendGrid(items = chartModel.legend)

                    AnalyticsHighlightCard(
                        percentLabel = chartModel.highlightLabel,
                        description = chartModel.highlightDescription
                    )

                    AnalyticsStatsRow(data = data)
                }
            }
        }
    }
}
