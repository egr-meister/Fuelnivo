package com.fuelnivo.app.ui.screens.refill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.MainViewModel
import com.fuelnivo.app.ui.components.ConfirmDialog
import com.fuelnivo.app.ui.components.EmptyState
import com.fuelnivo.app.ui.components.SectionLabel
import com.fuelnivo.app.ui.components.StatRow
import com.fuelnivo.app.ui.navigation.Routes
import com.fuelnivo.app.ui.theme.ValidCalculation
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.FuelCalculations
import com.fuelnivo.app.util.Formatting
import com.fuelnivo.app.util.IntervalReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefillDetailScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    navController: NavController,
    refillId: String?
) {
    val refill = state.refillById(refillId)
    val vehicle = refill?.let { state.vehicleById(it.vehicleId) }
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refill Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (refill != null) {
                        IconButton(onClick = { navController.navigate(Routes.editRefill(refill.id)) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit refill")
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete refill",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (refill == null || vehicle == null) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                title = "Refill not found",
                subtitle = "This record may have been deleted.",
                modifier = Modifier.fillMaxSize().padding(padding),
                action = { OutlinedButton(onClick = { navController.popBackStack() }) { Text("Go Back") } }
            )
            return@Scaffold
        }

        val currency = state.settings.currencyCode
        val asc = FuelCalculations.sortedAscending(state.refillsForVehicle(vehicle.id))
        val distanceSince = FuelCalculations.distanceSincePrevious(asc, refill)
        val interval = FuelCalculations.intervalEndingAt(vehicle, asc, refill)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = vehicle.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${DateUtils.longDisplay(refill.date)}${if (refill.time.isNotBlank()) " · ${refill.time}" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            SectionLabel("Record")
            StatRow("Odometer", Formatting.odometer(refill.odometer, vehicle.distanceUnit))
            StatRow(
                "Distance since previous",
                distanceSince?.let { Formatting.distance(it, vehicle.distanceUnit) } ?: "—"
            )
            StatRow("Fuel amount", Formatting.fuel(refill.fuelAmount, vehicle.fuelUnit))
            StatRow(
                "Total cost",
                refill.totalCost?.let { Formatting.cost(it, currency) } ?: "Not recorded"
            )
            StatRow(
                "Price per unit",
                refill.pricePerUnit?.let { Formatting.pricePerUnit(it, currency, vehicle.fuelUnit) }
                    ?: "Not recorded"
            )
            StatRow("Full tank", if (refill.fullTank) "Yes" else "No")
            StatRow("Missed previous refill", if (refill.missedPreviousRefill) "Yes" else "No")

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            SectionLabel("Interval consumption")
            Spacer(Modifier.height(4.dp))
            if (interval.isValid && interval.consumption != null) {
                Text(
                    text = Formatting.consumption(interval.consumption, vehicle.fuelUnit, vehicle.distanceUnit),
                    style = MaterialTheme.typography.titleLarge,
                    color = ValidCalculation
                )
                interval.distance?.let {
                    Text(
                        text = "Over ${Formatting.distance(it, vehicle.distanceUnit)}, " +
                            "${Formatting.fuel(interval.fuelUsed ?: 0.0, vehicle.fuelUnit)} used.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = reasonText(interval.reason),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (refill.note.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
                SectionLabel("Note")
                Spacer(Modifier.height(4.dp))
                Text(refill.note, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDelete && refill != null) {
        ConfirmDialog(
            title = "Delete this refill?",
            message = "This refill record will be permanently removed from this vehicle's log.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                showDelete = false
                viewModel.deleteRefill(refill.id)
                navController.popBackStack()
            },
            onDismiss = { showDelete = false }
        )
    }
}

private fun reasonText(reason: IntervalReason): String = when (reason) {
    IntervalReason.NOT_FULL_TANK -> "This record is not marked as a full tank."
    IntervalReason.NO_PREVIOUS_FULL_TANK -> "Previous full-tank record not found."
    IntervalReason.MISSED_REFILL -> "A missed refill interrupted this interval."
    IntervalReason.NON_POSITIVE_DISTANCE -> "Distance must be greater than zero."
    IntervalReason.VALID -> "More full-tank records are needed."
}
