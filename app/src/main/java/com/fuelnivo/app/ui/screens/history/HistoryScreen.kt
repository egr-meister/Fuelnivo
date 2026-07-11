package com.fuelnivo.app.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.data.Vehicle
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.MainViewModel
import com.fuelnivo.app.ui.components.ConfirmDialog
import com.fuelnivo.app.ui.components.DropdownSelector
import com.fuelnivo.app.ui.components.EmptyState
import com.fuelnivo.app.ui.navigation.Routes
import com.fuelnivo.app.ui.screens.home.VehicleSelector
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.FuelCalculations
import com.fuelnivo.app.util.Formatting
import java.time.YearMonth

private enum class HistoryFilter(val label: String) {
    All("All"), FullTank("Full tank"), CurrentMonth("This month")
}

private enum class HistorySort(val label: String) {
    NewestFirst("Newest first"),
    OldestFirst("Oldest first"),
    HighestFuel("Highest fuel"),
    HighestCost("Highest cost")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    navController: NavController
) {
    val vehicle = state.activeVehicle
    var filter by remember { mutableStateOf(HistoryFilter.All) }
    var sort by remember { mutableStateOf(HistorySort.NewestFirst) }
    var showReset by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refill History") },
                actions = {
                    if (vehicle != null && state.refillsForVehicle(vehicle.id).isNotEmpty()) {
                        IconButton(onClick = { showReset = true }) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = "Reset history",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (vehicle == null) {
            EmptyState(
                icon = Icons.Filled.LocalGasStation,
                title = "No vehicle selected",
                subtitle = "Add a vehicle to view its refill history.",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        val all = state.refillsForVehicle(vehicle.id)
        val filtered = applyFilter(all, filter)
        val sorted = applySort(filtered, sort)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            VehicleSelector(
                vehicles = state.vehicles,
                active = vehicle,
                onSelect = { viewModel.setActiveVehicle(it.id) }
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            DropdownSelector(
                label = "Sort",
                options = HistorySort.entries,
                selected = sort,
                optionLabel = { it.label },
                onSelected = { sort = it }
            )
            Spacer(Modifier.height(10.dp))

            if (sorted.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.LocalGasStation,
                    title = "No refill records yet.",
                    subtitle = "Add your first refill to begin the vehicle log.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sorted, key = { it.id }) { refill ->
                        HistoryRow(
                            refill = refill,
                            vehicle = vehicle,
                            currency = state.settings.currencyCode,
                            consumption = intervalLabel(vehicle, all, refill),
                            onClick = { navController.navigate(Routes.refillDetail(refill.id)) }
                        )
                    }
                }
            }
        }
    }

    if (showReset) {
        ConfirmDialog(
            title = "Reset this vehicle's history?",
            message = "All refill records for ${vehicle?.name ?: "this vehicle"} will be " +
                "permanently removed. The vehicle itself is kept.",
            confirmLabel = "Reset History",
            destructive = true,
            onConfirm = {
                showReset = false
                vehicle?.let { viewModel.deleteRefillsForVehicle(it.id) }
            },
            onDismiss = { showReset = false }
        )
    }
}

private fun applyFilter(refills: List<RefillRecord>, filter: HistoryFilter): List<RefillRecord> =
    when (filter) {
        HistoryFilter.All -> refills
        HistoryFilter.FullTank -> refills.filter { it.fullTank }
        HistoryFilter.CurrentMonth -> {
            val now = YearMonth.from(DateUtils.today())
            refills.filter { DateUtils.yearMonthOf(it.date) == now }
        }
    }

private fun applySort(refills: List<RefillRecord>, sort: HistorySort): List<RefillRecord> =
    when (sort) {
        HistorySort.NewestFirst -> FuelCalculations.sortedDescending(refills)
        HistorySort.OldestFirst -> FuelCalculations.sortedAscending(refills)
        HistorySort.HighestFuel -> refills.sortedByDescending { it.fuelAmount }
        HistorySort.HighestCost -> refills.sortedByDescending { it.totalCost ?: -1.0 }
    }

private fun intervalLabel(
    vehicle: Vehicle,
    all: List<RefillRecord>,
    refill: RefillRecord
): String? {
    if (!refill.fullTank) return null
    val asc = FuelCalculations.sortedAscending(all)
    val interval = FuelCalculations.intervalEndingAt(vehicle, asc, refill)
    val value = interval.consumption ?: return null
    return Formatting.consumption(value, vehicle.fuelUnit, vehicle.distanceUnit)
}

@Composable
private fun HistoryRow(
    refill: RefillRecord,
    vehicle: Vehicle,
    currency: String,
    consumption: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = DateUtils.longDisplay(refill.date),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${Formatting.fuelPlain(refill.fuelAmount)} ${vehicle.fuelUnit.suffix}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(2.dp))
        val parts = buildList {
            add(Formatting.odometer(refill.odometer, vehicle.distanceUnit))
            refill.totalCost?.let { add(Formatting.cost(it, currency)) }
            if (refill.fullTank) add("Full tank")
            consumption?.let { add(it) }
            if (refill.note.isNotBlank()) add("Note")
        }
        Text(
            text = parts.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
