package com.fuelnivo.app.ui.screens.refill

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.MainViewModel
import com.fuelnivo.app.ui.components.ConfirmDialog
import com.fuelnivo.app.ui.components.EmptyState
import com.fuelnivo.app.ui.components.LabeledTextField
import com.fuelnivo.app.ui.components.ToggleRow
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.Fields
import com.fuelnivo.app.util.Ids
import com.fuelnivo.app.util.NumberInput
import com.fuelnivo.app.util.Validation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRefillScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    navController: NavController,
    editingRefillId: String?
) {
    val editing = state.refillById(editingRefillId)
    val vehicle = editing?.let { state.vehicleById(it.vehicleId) } ?: state.activeVehicle

    if (vehicle == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Refill") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            EmptyState(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                title = "No vehicle available",
                subtitle = "Add a vehicle before recording a refill.",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        }
        return
    }

    val isEdit = editing != null

    var date by remember { mutableStateOf(editing?.date ?: DateUtils.todayString()) }
    var time by remember { mutableStateOf(editing?.time ?: DateUtils.nowTimeString()) }
    var odometer by remember {
        mutableStateOf(editing?.let { NumberInput.formatPlain(it.odometer, 0) } ?: "")
    }
    var fuelAmount by remember {
        mutableStateOf(editing?.let { NumberInput.formatPlain(it.fuelAmount, 2) } ?: "")
    }
    var totalCost by remember {
        mutableStateOf(editing?.totalCost?.let { NumberInput.formatPlain(it, 2) } ?: "")
    }
    var pricePerUnit by remember {
        mutableStateOf(editing?.pricePerUnit?.let { NumberInput.formatPlain(it, 3) } ?: "")
    }
    var fullTank by remember { mutableStateOf(editing?.fullTank ?: true) }
    var missed by remember { mutableStateOf(editing?.missedPreviousRefill ?: false) }
    var note by remember { mutableStateOf(editing?.note ?: "") }

    var costAutoFilled by remember { mutableStateOf(false) }
    var priceAutoFilled by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showOdometerWarning by remember { mutableStateOf(false) }

    // Recomputes the derived cost/price field without overwriting a value the
    // user manually edited.
    fun recompute() {
        val fuel = NumberInput.parseOrNull(fuelAmount)
        val cost = NumberInput.parseOrNull(totalCost)
        val price = NumberInput.parseOrNull(pricePerUnit)
        when {
            fuel != null && fuel > 0.0 && cost != null && (pricePerUnit.isBlank() || priceAutoFilled) -> {
                pricePerUnit = NumberInput.formatPlain(cost / fuel, 3)
                priceAutoFilled = true
            }
            fuel != null && fuel > 0.0 && price != null && (totalCost.isBlank() || costAutoFilled) -> {
                totalCost = NumberInput.formatPlain(fuel * price, 2)
                costAutoFilled = true
            }
        }
    }

    fun save() {
        val result = Validation.validateRefill(
            dateText = date,
            timeText = time,
            odometerText = odometer,
            fuelAmountText = fuelAmount,
            totalCostText = totalCost,
            pricePerUnitText = pricePerUnit,
            note = note
        )
        if (!result.isValid) {
            errors = result.errors
            return
        }
        errors = emptyMap()
        val odoValue = NumberInput.parseOrNull(odometer) ?: 0.0
        val outOfOrder = Validation.isOdometerOutOfOrder(
            newOdometer = odoValue,
            existingRefills = state.refillsForVehicle(vehicle.id),
            editingRefillId = editingRefillId
        )
        if (outOfOrder && !showOdometerWarning) {
            showOdometerWarning = true
            return
        }
        persist(vehicle.id, editing, date, time, odoValue, fuelAmount, totalCost, pricePerUnit,
            fullTank, missed, note, viewModel)
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Refill" else "Add Refill") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Vehicle: ${vehicle.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                LabeledTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Date (YYYY-MM-DD)",
                    error = errors[Fields.DATE],
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                LabeledTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = "Time (HH:mm)",
                    error = errors[Fields.TIME],
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = odometer,
                onValueChange = { if (NumberInput.isEditableDecimal(it)) odometer = it },
                label = "Odometer",
                suffix = vehicle.distanceUnit.suffix,
                keyboardType = KeyboardType.Decimal,
                error = errors[Fields.ODOMETER]
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = fuelAmount,
                onValueChange = {
                    if (NumberInput.isEditableDecimal(it)) {
                        fuelAmount = it
                        recompute()
                    }
                },
                label = "Fuel amount",
                suffix = vehicle.fuelUnit.suffix,
                keyboardType = KeyboardType.Decimal,
                error = errors[Fields.FUEL_AMOUNT]
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = totalCost,
                onValueChange = {
                    if (NumberInput.isEditableDecimal(it)) {
                        totalCost = it
                        costAutoFilled = false
                        recompute()
                    }
                },
                label = "Total cost (optional)",
                suffix = state.settings.currencyCode,
                keyboardType = KeyboardType.Decimal,
                error = errors[Fields.TOTAL_COST],
                supportingText = "Entering fuel amount and total cost fills in price per unit."
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = pricePerUnit,
                onValueChange = {
                    if (NumberInput.isEditableDecimal(it)) {
                        pricePerUnit = it
                        priceAutoFilled = false
                        recompute()
                    }
                },
                label = "Price per ${vehicle.fuelUnit.suffix} (optional)",
                suffix = state.settings.currencyCode,
                keyboardType = KeyboardType.Decimal,
                error = errors[Fields.PRICE_PER_UNIT],
                supportingText = "Entering fuel amount and price per unit fills in total cost."
            )
            Spacer(Modifier.height(8.dp))
            ToggleRow(
                title = "Full tank",
                subtitle = "Required for consumption calculations.",
                checked = fullTank,
                onCheckedChange = { fullTank = it }
            )
            ToggleRow(
                title = "Previous refill was missed",
                subtitle = "Excludes this interval from consumption calculations.",
                checked = missed,
                onCheckedChange = { missed = it }
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = note,
                onValueChange = { if (it.length <= Validation.MAX_NOTE_LENGTH) note = it },
                label = "Note (optional)",
                singleLine = false,
                error = errors[Fields.NOTE],
                supportingText = "${Validation.MAX_NOTE_LENGTH - note.length} characters left"
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = { save() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isEdit) "Save Changes" else "Save Refill")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showOdometerWarning) {
        ConfirmDialog(
            title = "Odometer looks lower than before",
            message = "The odometer value you entered is lower than a previous record for " +
                "this vehicle. Save it anyway? Your entered value will not be modified.",
            confirmLabel = "Save Anyway",
            onConfirm = {
                showOdometerWarning = false
                val odoValue = NumberInput.parseOrNull(odometer) ?: 0.0
                persist(vehicle.id, editing, date, time, odoValue, fuelAmount, totalCost,
                    pricePerUnit, fullTank, missed, note, viewModel)
                navController.popBackStack()
            },
            onDismiss = { showOdometerWarning = false }
        )
    }
}

private fun persist(
    vehicleId: String,
    editing: RefillRecord?,
    date: String,
    time: String,
    odometer: Double,
    fuelAmountText: String,
    totalCostText: String,
    pricePerUnitText: String,
    fullTank: Boolean,
    missed: Boolean,
    note: String,
    viewModel: MainViewModel
) {
    val now = DateUtils.todayString()
    val fuel = NumberInput.parseOrNull(fuelAmountText) ?: 0.0
    val cost = NumberInput.parseOrNull(totalCostText)
    val price = NumberInput.parseOrNull(pricePerUnitText)

    if (editing == null) {
        viewModel.addRefill(
            RefillRecord(
                id = Ids.newId(),
                vehicleId = vehicleId,
                date = date.trim(),
                time = time.trim(),
                odometer = odometer,
                fuelAmount = fuel,
                totalCost = cost,
                pricePerUnit = price,
                fullTank = fullTank,
                missedPreviousRefill = missed,
                note = note.trim(),
                createdAt = now,
                updatedAt = now
            )
        )
    } else {
        viewModel.updateRefill(
            editing.copy(
                date = date.trim(),
                time = time.trim(),
                odometer = odometer,
                fuelAmount = fuel,
                totalCost = cost,
                pricePerUnit = price,
                fullTank = fullTank,
                missedPreviousRefill = missed,
                note = note.trim(),
                updatedAt = now
            )
        )
    }
}
