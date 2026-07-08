package com.example.jhdkasjhd.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.jhdkasjhd.core.quickvntViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jhdkasjhd.ui.analytics.AnalyticsScreen
import com.example.jhdkasjhd.ui.auth.AuthViewModel
import com.example.jhdkasjhd.ui.auth.LoginScreen
import com.example.jhdkasjhd.ui.auth.RegisterScreen
import com.example.jhdkasjhd.ui.checkin.QrScannerScreen
import com.example.jhdkasjhd.ui.marketplace.CategoriesScreen
import com.example.jhdkasjhd.ui.marketplace.EventDetailScreen
import com.example.jhdkasjhd.ui.marketplace.MarketplaceScreen
import com.example.jhdkasjhd.ui.organizer.CreateEventScreen
import com.example.jhdkasjhd.ui.organizer.EditEventScreen
import com.example.jhdkasjhd.ui.organizer.MyEventsScreen
import com.example.jhdkasjhd.ui.profile.ProfileScreen
import com.example.jhdkasjhd.ui.splash.SplashScreen
import com.example.jhdkasjhd.ui.tickets.MyTicketsScreen
import com.example.jhdkasjhd.ui.tickets.RegisterEventScreen
import com.example.jhdkasjhd.ui.tickets.TicketDetailScreen
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceStrong
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private sealed class BottomTab(val route: String, val label: String) {
    data object Home : BottomTab(Routes.MARKETPLACE, "Inicio")
    data object Categories : BottomTab(Routes.CATEGORIES, "Categorías")
    data object Tickets : BottomTab(Routes.MY_TICKETS, "Boletos")
    data object MyEvents : BottomTab(Routes.MY_EVENTS, "Mis Eventos")
    data object Profile : BottomTab(Routes.PROFILE, "Perfil")
}

@Composable
fun QuickvntNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = quickvntViewModel()
) {
    val session by authViewModel.session.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = session != null && currentRoute in setOf(
        Routes.MARKETPLACE,
        Routes.CATEGORIES,
        Routes.MY_TICKETS,
        Routes.MY_EVENTS,
        Routes.PROFILE
    ) || (currentRoute?.startsWith("marketplace/category/") == true)

    Scaffold(
        containerColor = CoinbaseCanvas,
        bottomBar = {
            if (showBottomBar && session != null) {
                val tabs = if (session!!.isOrganizer) {
                    listOf(BottomTab.MyEvents, BottomTab.Home, BottomTab.Profile)
                } else {
                    listOf(BottomTab.Home, BottomTab.Categories, BottomTab.Tickets, BottomTab.Profile)
                }

                NavigationBar(
                    containerColor = CoinbaseCanvas,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = when (tab) {
                                BottomTab.Home -> currentRoute == Routes.MARKETPLACE ||
                                    currentRoute?.startsWith("marketplace/category/") == true
                                BottomTab.Categories -> currentRoute == Routes.CATEGORIES
                                else -> currentRoute == tab.route
                            },
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CoinbasePrimary,
                                selectedTextColor = CoinbasePrimary,
                                unselectedIconColor = CoinbaseMuted,
                                unselectedTextColor = CoinbaseMuted,
                                indicatorColor = CoinbaseSurfaceStrong
                            ),
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        BottomTab.Home -> Icons.Default.Home
                                        BottomTab.Categories -> Icons.Default.GridView
                                        BottomTab.Tickets -> Icons.Default.Event
                                        BottomTab.MyEvents -> Icons.Default.Event
                                        BottomTab.Profile -> Icons.Default.Person
                                    },
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onFinished = {
                        val destination = if (session != null) {
                            if (session!!.isOrganizer) Routes.MY_EVENTS else Routes.MARKETPLACE
                        } else {
                            Routes.LOGIN
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateRegister = { navController.navigate(Routes.REGISTER) },
                    onLoginSuccess = { isOrganizer ->
                        val dest = if (isOrganizer) Routes.MY_EVENTS else Routes.MARKETPLACE
                        navController.navigate(dest) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateLogin = { navController.popBackStack() },
                    onRegisterSuccess = { isOrganizer ->
                        val dest = if (isOrganizer) Routes.MY_EVENTS else Routes.MARKETPLACE
                        navController.navigate(dest) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.MARKETPLACE) {
                MarketplaceScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Routes.eventDetail(eventId))
                    },
                    onSeeAllCategories = { navController.navigate(Routes.CATEGORIES) },
                    onCategoryClick = { category ->
                        navController.navigate(Routes.marketplaceCategory(category))
                    }
                )
            }

            composable(
                route = Routes.MARKETPLACE_CATEGORY,
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStack ->
                val categoryName = backStack.arguments?.getString("categoryName")?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                MarketplaceScreen(
                    initialCategory = categoryName,
                    onEventClick = { eventId ->
                        navController.navigate(Routes.eventDetail(eventId))
                    },
                    onSeeAllCategories = { navController.navigate(Routes.CATEGORIES) },
                    onCategoryClick = { category ->
                        navController.navigate(Routes.marketplaceCategory(category))
                    }
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    onCategoryClick = { category ->
                        navController.navigate(Routes.marketplaceCategory(category))
                    }
                )
            }

            composable(
                route = Routes.EVENT_DETAIL,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStack ->
                val eventId = backStack.arguments?.getString("eventId").orEmpty()
                EventDetailScreen(
                    eventId = eventId,
                    isOrganizer = session?.isOrganizer == true,
                    onRegisterClick = { navController.navigate(Routes.registerEvent(eventId)) },
                    onAnalyticsClick = { navController.navigate(Routes.analytics(eventId)) },
                    onScanClick = { navController.navigate(Routes.qrScanner(eventId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.REGISTER_EVENT,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStack ->
                val eventId = backStack.arguments?.getString("eventId").orEmpty()
                RegisterEventScreen(
                    eventId = eventId,
                    onSuccess = { ticketId ->
                        navController.navigate(Routes.ticketDetail(ticketId)) {
                            popUpTo(Routes.MARKETPLACE)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.MY_TICKETS) {
                MyTicketsScreen(
                    onTicketClick = { ticketId ->
                        navController.navigate(Routes.ticketDetail(ticketId))
                    }
                )
            }

            composable(
                route = Routes.TICKET_DETAIL,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
            ) { backStack ->
                val ticketId = backStack.arguments?.getString("ticketId").orEmpty()
                TicketDetailScreen(
                    ticketId = ticketId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.MY_EVENTS) {
                MyEventsScreen(
                    onCreateEvent = { navController.navigate(Routes.CREATE_EVENT) },
                    onEditEvent = { eventId -> navController.navigate(Routes.editEvent(eventId)) },
                    onAnalytics = { eventId -> navController.navigate(Routes.analytics(eventId)) },
                    onScan = { eventId -> navController.navigate(Routes.qrScanner(eventId)) }
                )
            }

            composable(Routes.CREATE_EVENT) {
                CreateEventScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EDIT_EVENT,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStack ->
                val eventId = backStack.arguments?.getString("eventId").orEmpty()
                EditEventScreen(
                    eventId = eventId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.ANALYTICS,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStack ->
                val eventId = backStack.arguments?.getString("eventId").orEmpty()
                AnalyticsScreen(
                    eventId = eventId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.QR_SCANNER,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStack ->
                val eventId = backStack.arguments?.getString("eventId").orEmpty()
                QrScannerScreen(
                    eventId = eventId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
