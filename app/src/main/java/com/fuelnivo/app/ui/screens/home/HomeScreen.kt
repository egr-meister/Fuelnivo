package com.fuelnivo.app.ui.screens.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.MainViewModel
import com.fuelnivo.app.ui.components.EmptyState
import com.fuelnivo.app.ui.components.FuelTankView
import com.fuelnivo.app.ui.components.OutlinedValue
import com.fuelnivo.app.ui.components.SectionLabel
import com.fuelnivo.app.ui.navigation.Routes
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.DisclaimerText
import com.fuelnivo.app.util.FuelCalculations
import com.fuelnivo.app.util.Formatting
import java.time.YearMonth

@Composable
fun HomeScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    navController: NavController
) {
    val vehicle = state.activeVehicle

    Scaffold(
        floatingActionButton = {
            if (vehicle != null) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Routes.ADD_REFILL) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add Refill") }
                )
            }
        }
    ) { padding ->
        if (vehicle == null) {
            NoVehicleHome(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddVehicle = { navController.navigate(Routes.ADD_VEHICLE) }
            )
            return@Scaffold
        }

        val refills = state.refillsForVehicle(vehicle.id)
        val latest = FuelCalculations.sortedDescending(refills).firstOrNull()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            VehicleSelector(
                vehicles = state.vehicles,
                active = vehicle,
                onSelect = { viewModel.setActiveVehicle(it.id) }
            )

            Spacer(Modifier.height(8.dp))
            ReminderPanel(
                state = state.reminderState,
                onAddRefill = { navController.navigate(Routes.ADD_REFILL) },
                onDismiss = { viewModel.dismissReminder() }
            )

            // --- Fuel tank + latest refill (asymmetrical layout) ------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val capacity = vehicle.tankCapacity
                val amount = latest?.fuelAmount ?: 0.0
                val fraction = if (capacity > 0.0) (amount / capacity).toFloat() else 0f
                val overCapacity = capacity > 0.0 && amount > capacity

                FuelTankView(
                    fillFraction = fraction,
                    percentLabel = Formatting.percent(if (capacity > 0) amount / capacity else 0.0),
                    overCapacity = overCapacity,
                    isEmpty = latest == null,
                    contentDescription = if (latest == null)
                        "Empty fuel tank illustration. No refill recorded yet."
                    else
                        "Fuel tank showing ${Formatting.percent(amount / capacity)} of configured capacity based on your latest manual refill.",
                    width = 150.dp,
                    height = 200.dp
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("Latest manual refill")
                    Spacer(Modifier.height(4.dp))
                    if (latest == null) {
                        Text(
                            text = "No refill recorded yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Add a manual refill to start your log.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { navController.navigate(Routes.ADD_REFILL) }) {
                            Text("Record First Refill")
                        }
                    } else {
                        Text(
                            text = "${Formatting.fuelPlain(latest.fuelAmount)} ${vehicle.fuelUnit.suffix} of ${Formatting.capacity(capacity, vehicle.fuelUnit)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Formatting.percent(amount / capacity) + " of configured capacity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Last refill: ${DateUtils.shortDisplay(latest.date)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (overCapacity) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Recorded amount exceeds configured tank capacity. The tank visual is capped at 100%.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Text(
                text = DisclaimerText.TANK_NOTE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            // --- Narrow mileage strip ---------------------------------------
            Spacer(Modifier.height(16.dp))
            MileageStrip(state, vehicle.id)

            // --- Monthly summary rail ---------------------------------------
            Spacer(Modifier.height(20.dp))
            SectionLabel("This month")
            Spacer(Modifier.height(8.dp))
            MonthlyRail(state, vehicle)

            // --- Recent refill timeline -------------------------------------
            Spacer(Modifier.height(20.dp))
            SectionLabel("Recent refills")
            Spacer(Modifier.height(8.dp))
            if (refills.isEmpty()) {
                Text(
                    text = "No refills recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                RecentTimeline(state, vehicle, onOpen = {
                    navController.navigate(Routes.refillDetail(it))
                })
            }
        }
    }
}

@Composable
private fun MileageStrip(state: AppUiState, vehicleId: String) {
    val vehicle = state.vehicleById(vehicleId) ?: return
    val refills = state.refillsForVehicle(vehicleId)
    val current = FuelCalculations.currentOdometer(refills)
    val consumption = FuelCalculations.latestConsumption(vehicle, refills)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedValue(
            label = "Odometer",
            value = current?.let { Formatting.odometer(it, vehicle.distanceUnit) } ?: "—",
            modifier = Modifier.weight(1f)
        )
        OutlinedValue(
            label = Formatting.consumptionUnitLabel(vehicle.fuelUnit, vehicle.distanceUnit),
            value = consumption?.let {
                Formatting.consumption(it, vehicle.fuelUnit, vehicle.distanceUnit)
                    .substringBefore(" ")
            } ?: "—",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MonthlyRail(state: AppUiState, vehicle: com.fuelnivo.app.data.Vehicle) {
    val summary = com.fuelnivo.app.util.Statistics.monthlySummary(
        vehicle = vehicle,
        refills = state.refillsForVehicle(vehicle.id),
        month = YearMonth.from(DateUtils.today())
    )
    val currency = state.settings.currencyCode
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedValue("Refills", summary.refillCount.toString())
        OutlinedValue(
            "Fuel",
            "${Formatting.fuelPlain(summary.totalFuel)} ${vehicle.fuelUnit.suffix}"
        )
        OutlinedValue(
            "Recorded cost",
            summary.totalCost?.let { Formatting.cost(it, currency) } ?: "—"
        )
        OutlinedValue(
            "Distance",
            summary.totalDistance?.let { Formatting.distance(it, vehicle.distanceUnit) } ?: "—"
        )
    }
}

@Composable
private fun NoVehicleHome(modifier: Modifier, onAddVehicle: () -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FuelTankView(
            fillFraction = 0f,
            percentLabel = "",
            overCapacity = false,
            isEmpty = true,
            contentDescription = "Empty fuel tank outline",
            width = 150.dp,
            height = 190.dp
        )
        Spacer(Modifier.height(16.dp))
        EmptyState(
            icon = Icons.Filled.DirectionsCar,
            title = "Add your first vehicle",
            subtitle = "Fuelnivo is a manual fuel log. Set up a vehicle to begin recording refills.",
            action = {
                Button(onClick = onAddVehicle) { Text("Add Vehicle") }
            }
        )
    }
}
