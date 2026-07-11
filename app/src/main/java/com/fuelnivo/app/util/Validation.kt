package com.fuelnivo.app.util

import com.fuelnivo.app.data.RefillRecord

/** Result of validating a form; either valid or carrying field errors. */
data class ValidationResult(
    val errors: Map<String, String> = emptyMap()
) {
    val isValid: Boolean get() = errors.isEmpty()
    fun errorFor(field: String): String? = errors[field]
}

/** Field key constants shared between screens and validators. */
object Fields {
    const val NAME = "name"
    const val TANK_CAPACITY = "tankCapacity"
    const val YEAR = "year"
    const val INITIAL_ODOMETER = "initialOdometer"
    const val NOTE = "note"
    const val DATE = "date"
    const val TIME = "time"
    const val ODOMETER = "odometer"
    const val FUEL_AMOUNT = "fuelAmount"
    const val TOTAL_COST = "totalCost"
    const val PRICE_PER_UNIT = "pricePerUnit"
}

object Validation {

    // Generous safety ceilings to reject clearly invalid input without
    // constraining legitimate values.
    const val MAX_FUEL_AMOUNT = 100_000.0
    const val MAX_TANK_CAPACITY = 100_000.0
    const val MAX_ODOMETER = 100_000_000.0
    const val MAX_COST = 100_000_000.0
    const val MAX_NOTE_LENGTH = 300

    fun validateVehicle(
        name: String,
        tankCapacityText: String,
        yearText: String,
        initialOdometerText: String,
        note: String
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (name.trim().isEmpty()) {
            errors[Fields.NAME] = "Vehicle name is required."
        }

        val capacity = NumberInput.parseOrNull(tankCapacityText)
        when {
            tankCapacityText.isBlank() -> errors[Fields.TANK_CAPACITY] = "Tank capacity is required."
            capacity == null -> errors[Fields.TANK_CAPACITY] = "Enter a valid number."
            capacity <= 0.0 -> errors[Fields.TANK_CAPACITY] = "Capacity must be greater than zero."
            capacity > MAX_TANK_CAPACITY -> errors[Fields.TANK_CAPACITY] = "Capacity is too large."
        }

        if (yearText.isNotBlank()) {
            val year = NumberInput.parseIntOrNull(yearText)
            if (year == null || year < 1900 || year > 2100) {
                errors[Fields.YEAR] = "Enter a year between 1900 and 2100."
            }
        }

        if (initialOdometerText.isNotBlank()) {
            val odo = NumberInput.parseOrNull(initialOdometerText)
            if (odo == null || odo < 0.0 || odo > MAX_ODOMETER) {
                errors[Fields.INITIAL_ODOMETER] = "Enter a valid odometer value."
            }
        }

        if (note.length > MAX_NOTE_LENGTH) {
            errors[Fields.NOTE] = "Note must be $MAX_NOTE_LENGTH characters or fewer."
        }

        return ValidationResult(errors)
    }

    /**
     * Validates a refill form. Odometer-below-previous is not a hard error; it
     * is surfaced separately as a warning that requires explicit confirmation.
     */
    fun validateRefill(
        dateText: String,
        timeText: String,
        odometerText: String,
        fuelAmountText: String,
        totalCostText: String,
        pricePerUnitText: String,
        note: String
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (!DateUtils.isValidDateString(dateText)) {
            errors[Fields.DATE] = "Enter a valid date."
        }
        if (!DateUtils.isValidTimeString(timeText)) {
            errors[Fields.TIME] = "Enter a valid time (HH:mm)."
        }

        val odometer = NumberInput.parseOrNull(odometerText)
        when {
            odometerText.isBlank() -> errors[Fields.ODOMETER] = "Odometer is required."
            odometer == null -> errors[Fields.ODOMETER] = "Enter a valid number."
            odometer < 0.0 -> errors[Fields.ODOMETER] = "Odometer cannot be negative."
            odometer > MAX_ODOMETER -> errors[Fields.ODOMETER] = "Odometer is too large."
        }

        val fuel = NumberInput.parseOrNull(fuelAmountText)
        when {
            fuelAmountText.isBlank() -> errors[Fields.FUEL_AMOUNT] = "Fuel amount is required."
            fuel == null -> errors[Fields.FUEL_AMOUNT] = "Enter a valid number."
            fuel <= 0.0 -> errors[Fields.FUEL_AMOUNT] = "Fuel amount must be greater than zero."
            fuel > MAX_FUEL_AMOUNT -> errors[Fields.FUEL_AMOUNT] = "Fuel amount is too large."
        }

        if (totalCostText.isNotBlank()) {
            val cost = NumberInput.parseOrNull(totalCostText)
            if (cost == null || cost < 0.0 || cost > MAX_COST) {
                errors[Fields.TOTAL_COST] = "Enter a valid non-negative cost."
            }
        }

        if (pricePerUnitText.isNotBlank()) {
            val price = NumberInput.parseOrNull(pricePerUnitText)
            if (price == null || price < 0.0 || price > MAX_COST) {
                errors[Fields.PRICE_PER_UNIT] = "Enter a valid non-negative price."
            }
        }

        if (note.length > MAX_NOTE_LENGTH) {
            errors[Fields.NOTE] = "Note must be $MAX_NOTE_LENGTH characters or fewer."
        }

        return ValidationResult(errors)
    }

    /** True when the new odometer is lower than the latest existing refill. */
    fun isOdometerOutOfOrder(
        newOdometer: Double,
        existingRefills: List<RefillRecord>,
        editingRefillId: String?
    ): Boolean {
        val previous = existingRefills
            .filter { it.id != editingRefillId }
            .maxByOrNull { it.odometer }
            ?: return false
        return newOdometer < previous.odometer
    }
}
