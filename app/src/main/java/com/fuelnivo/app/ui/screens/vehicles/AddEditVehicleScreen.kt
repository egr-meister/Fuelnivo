package com.fuelnivo.app.ui.screens.vehicles

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
import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.FuelType
import com.fuelnivo.app.data.FuelUnit
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.data.Vehicle
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.MainViewModel
import com.fuelnivo.app.ui.components.ConfirmDialog
import com.fuelnivo.app.ui.components.DropdownSelector
import com.fuelnivo.app.ui.components.LabeledTextField
import com.fuelnivo.app.util.Conversions
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.Fields
import com.fuelnivo.app.util.Ids
import com.fuelnivo.app.util.NumberInput
import com.fuelnivo.app.util.Validation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    navController: NavController,
    editingVehicleId: String?
) {
    val editing = state.vehicleById(editingVehicleId)
    val isEdit = editing != null

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var manufacturer by remember { mutableStateOf(editing?.manufacturer ?: "") }
    var model by remember { mutableStateOf(editing?.model ?: "") }
    var year by remember { mutableStateOf(editing?.year?.toString() ?: "") }
    var fuelType by remember { mutableStateOf(editing?.fuelType ?: FuelType.Gasoline) }
    var tankCapacity by remember {
        mutableStateOf(editing?.let { NumberInput.formatPlain(it.tankCapacity, 1) } ?: "")
    }
    var fuelUnit by remember { mutableStateOf(editing?.fuelUnit ?: FuelUnit.Liters) }
    var distanceUnit by remember { mutableStateOf(editing?.distanceUnit ?: DistanceUnit.Kilometers) }
    var initialOdometer by remember {
        mutableStateOf(editing?.initialOdometer?.let { NumberInput.formatPlain(it, 0) } ?: "")
    }
    var note by remember { mutableStateOf(editing?.note ?: "") }

    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showConversionDialog by remember { mutableStateOf(false) }

    val recordsCount = if (isEdit) state.refillsForVehicle(editing!!.id).size else 0

    fun buildValidation() = Validation.validateVehicle(
        name = name,
        tankCapacityText = tankCapacity,
        yearText = year,
        initialOdometerText = initialOdometer,
        note = note
    )

    fun persist(convertRecords: Boolean) {
        val now = DateUtils.todayString()
        val capacityValue = NumberInput.parseOrNull(tankCapacity) ?: 0.0
        val odoValue = NumberInput.parseOrNull(initialOdometer)

        if (editing == null) {
            val vehicle = Vehicle(
                id = Ids.newId(),
                name = name.trim(),
                manufacturer = manufacturer.trim(),
                model = model.trim(),
                year = NumberInput.parseIntOrNull(year),
                fuelType = fuelType,
                tankCapacity = capacityValue,
                fuelUnit = fuelUnit,
                distanceUnit = distanceUnit,
                initialOdometer = odoValue,
                note = note.trim(),
                createdAt = now,
                updatedAt = now
            )
            viewModel.addVehicle(vehicle)
        } else {
            val fuelChanged = fuelUnit != editing.fuelUnit
            val distanceChanged = distanceUnit != editing.distanceUnit

            var finalCapacity = capacityValue
            var finalInitialOdo = odoValue
            // When the user typed capacity/odometer they are in the NEW units already,
            // so we only convert stored refill values, not the freshly typed fields.
            val updatedVehicle = editing.copy(
                name = name.trim(),
                manufacturer = manufacturer.trim(),
                model = model.trim(),
                year = NumberInput.parseIntOrNull(year),
                fuelType = fuelType,
                tankCapacity = finalCapacity,
                fuelUnit = fuelUnit,
                distanceUnit = distanceUnit,
                initialOdometer = finalInitialOdo,
                note = note.trim(),
                updatedAt = now
            )

            if (convertRecords && (fuelChanged || distanceChanged)) {
                val converted = state.refillsForVehicle(editing.id).map { r ->
                    r.copy(
                        fuelAmount = if (fuelChanged)
                            Conversions.convertFuel(r.fuelAmount, editing.fuelUnit, fuelUnit)
                        else r.fuelAmount,
                        pricePerUnit = if (fuelChanged && r.pricePerUnit != null)
                            Conversions.convertPricePerUnit(r.pricePerUnit, editing.fuelUnit, fuelUnit)
                        else r.pricePerUnit,
                        odometer = if (distanceChanged)
                            Conversions.convertDistance(r.odometer, editing.distanceUnit, distanceUnit)
                        else r.odometer,
                        updatedAt = now
                    )
                }
                viewModel.replaceVehicleAndRefills(updatedVehicle, converted)
            } else {
                viewModel.updateVehicle(updatedVehicle)
            }
        }
        navController.popBackStack()
    }

    fun onSave() {
        val result = buildValidation()
        if (!result.isValid) {
            errors = result.errors
            return
        }
        errors = emptyMap()
        val unitsChanged = isEdit &&
            (fuelUnit != editing!!.fuelUnit || distanceUnit != editing.distanceUnit)
        if (unitsChanged && recordsCount > 0) {
            showConversionDialog = true
        } else {
            persist(convertRecords = false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Vehicle" else "Set Up Vehicle") },
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
            LabeledTextField(
                value = name,
                onValueChange = { name = it },
                label = "Vehicle name",
                error = errors[Fields.NAME]
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = manufacturer,
                onValueChange = { manufacturer = it },
                label = "Manufacturer (optional)"
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = model,
                onValueChange = { model = it },
                label = "Model (optional)"
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = year,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) year = it },
                label = "Year (optional)",
                keyboardType = KeyboardType.Number,
                error = errors[Fields.YEAR]
            )
            Spacer(Modifier.height(12.dp))
            DropdownSelector(
                label = "Fuel type",
                options = FuelType.entries,
                selected = fuelType,
                optionLabel = { it.label },
                onSelected = { fuelType = it }
            )
            Spacer(Modifier.height(12.dp))
            DropdownSelector(
                label = "Fuel unit",
                options = FuelUnit.entries,
                selected = fuelUnit,
                optionLabel = { it.label },
                onSelected = { fuelUnit = it }
            )
            Spacer(Modifier.height(12.dp))
            DropdownSelector(
                label = "Distance unit",
                options = DistanceUnit.entries,
                selected = distanceUnit,
                optionLabel = { it.label },
                onSelected = { distanceUnit = it }
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = tankCapacity,
                onValueChange = { if (NumberInput.isEditableDecimal(it)) tankCapacity = it },
                label = "Tank capacity",
                suffix = fuelUnit.suffix,
                keyboardType = KeyboardType.Decimal,
                error = errors[Fields.TANK_CAPACITY]
            )
            Spacer(Modifier.height(12.dp))
            LabeledTextField(
                value = initialOdometer,
                onValueChange = { if (NumberInput.isEditableDecimal(it)) initialOdometer = it },
                label = "Initial odometer (optional)",
                suffix = distanceUnit.suffix,
                keyboardType = KeyboardType.Decimal,
                error = errors[Fields.INITIAL_ODOMETER]
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
            Button(onClick = { onSave() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isEdit) "Save Changes" else "Save Vehicle")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showConversionDialog && editing != null) {
        ConfirmDialog(
            title = "Convert existing records?",
            message = "You changed this vehicle's units. All $recordsCount stored refill " +
                "records will be converted to the new units, preserving decimal precision. " +
                "Existing values are never reinterpreted without conversion.",
            confirmLabel = "Convert and Save",
            onConfirm = {
                showConversionDialog = false
                persist(convertRecords = true)
            },
            onDismiss = { showConversionDialog = false }
        )
    }
}
