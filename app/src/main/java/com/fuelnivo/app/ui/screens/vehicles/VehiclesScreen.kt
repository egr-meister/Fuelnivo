package com.fuelnivo.app.ui.screens.vehicles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.data.Vehicle
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.MainViewModel
import com.fuelnivo.app.ui.components.ConfirmDialog
import com.fuelnivo.app.ui.components.EmptyState
import com.fuelnivo.app.ui.navigation.Routes
import com.fuelnivo.app.util.FuelCalculations
import com.fuelnivo.app.util.Formatting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    navController: NavController
) {
    var pendingDelete by remember { mutableStateOf<Vehicle?>(null) }
    val activeId = state.activeVehicle?.id

    Scaffold(
        topBar = { TopAppBar(title = { Text("Vehicles") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_VEHICLE) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add vehicle")
            }
        }
    ) { padding ->
        if (state.vehicles.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.DirectionsCar,
                title = "No vehicles yet",
                subtitle = "Add your first vehicle to start a manual fuel log.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            DeleteDialog(pendingDelete, onConfirm = {}, onDismiss = { pendingDelete = null })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.vehicles, key = { it.id }) { vehicle ->
                VehiclePlate(
                    vehicle = vehicle,
                    isActive = vehicle.id == activeId,
                    refillCount = state.refillsForVehicle(vehicle.id).size,
                    currentOdometer = FuelCalculations.currentOdometer(
                        state.refillsForVehicle(vehicle.id)
                    ),
                    onSelect = { viewModel.setActiveVehicle(vehicle.id) },
                    onEdit = { navController.navigate(Routes.editVehicle(vehicle.id)) },
                    onDelete = { pendingDelete = vehicle }
                )
            }
        }
    }

    DeleteDialog(
        pendingDelete,
        onConfirm = {
            pendingDelete?.let { viewModel.deleteVehicle(it.id) }
            pendingDelete = null
        },
        onDismiss = { pendingDelete = null }
    )
}

@Composable
private fun DeleteDialog(vehicle: Vehicle?, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    if (vehicle != null) {
        ConfirmDialog(
            title = "Delete this vehicle?",
            message = "This will also remove all refill records stored for this vehicle.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun VehiclePlate(
    vehicle: Vehicle,
    isActive: Boolean,
    refillCount: Int,
    currentOdometer: Double?,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isActive) 2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isActive) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Active vehicle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }
                val makeModel = listOf(vehicle.manufacturer, vehicle.model)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                if (makeModel.isNotBlank()) {
                    Text(
                        text = makeModel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit vehicle")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete vehicle",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PlateFact("Units", "${vehicle.fuelUnit.suffix} / ${vehicle.distanceUnit.suffix}")
            PlateFact("Refills", refillCount.toString())
            PlateFact(
                "Odometer",
                currentOdometer?.let { Formatting.odometer(it, vehicle.distanceUnit) } ?: "—"
            )
        }
    }
}

@Composable
private fun PlateFact(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
