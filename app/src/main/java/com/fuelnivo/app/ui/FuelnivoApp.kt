package com.fuelnivo.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.fuelnivo.app.data.FuelnivoRepository
import com.fuelnivo.app.ui.navigation.BottomDestination
import com.fuelnivo.app.ui.navigation.Routes
import com.fuelnivo.app.ui.screens.history.HistoryScreen
import com.fuelnivo.app.ui.screens.home.HomeScreen
import com.fuelnivo.app.ui.screens.monthly.MonthlyStatisticsScreen
import com.fuelnivo.app.ui.screens.onboarding.OnboardingScreen
import com.fuelnivo.app.ui.screens.refill.AddEditRefillScreen
import com.fuelnivo.app.ui.screens.refill.RefillDetailScreen
import com.fuelnivo.app.ui.screens.settings.SettingsScreen
import com.fuelnivo.app.ui.screens.statistics.StatisticsScreen
import com.fuelnivo.app.ui.screens.vehicles.AddEditVehicleScreen
import com.fuelnivo.app.ui.screens.vehicles.VehiclesScreen

@Composable
fun FuelnivoApp(repository: FuelnivoRepository) {
    val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    // Remembers that the user chose "Set Up Vehicle" during onboarding. The
    // navigation is deferred until the NavHost below is actually composed,
    // because navigating on a NavController with no graph yet would crash.
    var pendingVehicleSetup by remember { mutableStateOf(false) }

    if (!state.loaded) return

    if (!state.settings.onboardingCompleted) {
        OnboardingScreen(
            onFinish = { setUpVehicle ->
                pendingVehicleSetup = setUpVehicle
                viewModel.completeOnboarding()
            }
        )
        return
    }

    // Once the main UI (and its NavHost) is present, honor a pending request to
    // open the Add Vehicle screen exactly once.
    LaunchedEffect(pendingVehicleSetup) {
        if (pendingVehicleSetup) {
            pendingVehicleSetup = false
            navController.navigate(Routes.ADD_VEHICLE)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = BottomDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomDestination.entries.forEach { dest ->
                        val selected = backStackEntry?.destination?.hierarchy?.any {
                            it.route == dest.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(iconFor(dest), contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(state = state, viewModel = viewModel, navController = navController)
            }
            composable(Routes.HISTORY) {
                HistoryScreen(state = state, viewModel = viewModel, navController = navController)
            }
            composable(Routes.STATISTICS) {
                StatisticsScreen(state = state, navController = navController)
            }
            composable(Routes.VEHICLES) {
                VehiclesScreen(state = state, viewModel = viewModel, navController = navController)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(state = state, viewModel = viewModel, navController = navController)
            }
            composable(Routes.MONTHLY) {
                MonthlyStatisticsScreen(state = state, navController = navController)
            }
            composable(Routes.ADD_REFILL) {
                AddEditRefillScreen(
                    state = state,
                    viewModel = viewModel,
                    navController = navController,
                    editingRefillId = null
                )
            }
            composable(
                route = "${Routes.EDIT_REFILL}/{${Routes.ARG_REFILL_ID}}",
                arguments = listOf(navArgument(Routes.ARG_REFILL_ID) { type = NavType.StringType })
            ) { entry ->
                AddEditRefillScreen(
                    state = state,
                    viewModel = viewModel,
                    navController = navController,
                    editingRefillId = entry.arguments?.getString(Routes.ARG_REFILL_ID)
                )
            }
            composable(
                route = "${Routes.REFILL_DETAIL}/{${Routes.ARG_REFILL_ID}}",
                arguments = listOf(navArgument(Routes.ARG_REFILL_ID) { type = NavType.StringType })
            ) { entry ->
                RefillDetailScreen(
                    state = state,
                    viewModel = viewModel,
                    navController = navController,
                    refillId = entry.arguments?.getString(Routes.ARG_REFILL_ID)
                )
            }
            composable(Routes.ADD_VEHICLE) {
                AddEditVehicleScreen(
                    state = state,
                    viewModel = viewModel,
                    navController = navController,
                    editingVehicleId = null
                )
            }
            composable(
                route = "${Routes.EDIT_VEHICLE}/{${Routes.ARG_VEHICLE_ID}}",
                arguments = listOf(navArgument(Routes.ARG_VEHICLE_ID) { type = NavType.StringType })
            ) { entry ->
                AddEditVehicleScreen(
                    state = state,
                    viewModel = viewModel,
                    navController = navController,
                    editingVehicleId = entry.arguments?.getString(Routes.ARG_VEHICLE_ID)
                )
            }
        }
    }
}

private fun iconFor(dest: BottomDestination): ImageVector = when (dest) {
    BottomDestination.Home -> Icons.Filled.Home
    BottomDestination.History -> Icons.AutoMirrored.Filled.List
    BottomDestination.Statistics -> Icons.Filled.BarChart
    BottomDestination.Vehicles -> Icons.Filled.DirectionsCar
    BottomDestination.Settings -> Icons.Filled.Settings
}
