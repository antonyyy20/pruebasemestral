package com.example.jhdkasjhd.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jhdkasjhd.QuickvntApplication
import com.example.jhdkasjhd.ui.analytics.AnalyticsViewModel
import com.example.jhdkasjhd.ui.auth.AuthViewModel
import com.example.jhdkasjhd.ui.checkin.CheckinViewModel
import com.example.jhdkasjhd.ui.marketplace.MarketplaceViewModel
import com.example.jhdkasjhd.ui.organizer.OrganizerViewModel
import com.example.jhdkasjhd.ui.tickets.TicketsViewModel

class QuickvntViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(container.authRepository) as T
            modelClass.isAssignableFrom(MarketplaceViewModel::class.java) ->
                MarketplaceViewModel(container.eventRepository) as T
            modelClass.isAssignableFrom(TicketsViewModel::class.java) ->
                TicketsViewModel(container.ticketRepository, container.eventRepository) as T
            modelClass.isAssignableFrom(OrganizerViewModel::class.java) ->
                OrganizerViewModel(container.eventRepository, container.authRepository) as T
            modelClass.isAssignableFrom(CheckinViewModel::class.java) ->
                CheckinViewModel(container.ticketRepository) as T
            modelClass.isAssignableFrom(AnalyticsViewModel::class.java) ->
                AnalyticsViewModel(container.ticketRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}

@Composable
fun quickvntViewModelFactory(): QuickvntViewModelFactory {
    val app = LocalContext.current.applicationContext as QuickvntApplication
    return QuickvntViewModelFactory(app.container)
}

@Composable
inline fun <reified VM : ViewModel> quickvntViewModel(): VM {
    return viewModel(factory = quickvntViewModelFactory())
}
