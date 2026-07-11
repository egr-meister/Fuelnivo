package com.fuelnivo.app.util

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.FuelUnit
import java.util.Locale

/**
 * Display formatting. Numeric values are stored as numbers and formatted only
 * here. Rounding precision follows the product specification.
 */
object Formatting {

    private fun round(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)

    fun fuel(amount: Double, unit: FuelUnit): String =
        "${round(amount, 2)} ${unit.suffix}"

    fun fuelPlain(amount: Double): String = round(amount, 2)

    fun capacity(value: Double, unit: FuelUnit): String =
        "${round(value, 1)} ${unit.suffix}"

    fun distance(value: Double, unit: DistanceUnit): String =
        "${round(value, 1)} ${unit.suffix}"

    fun odometer(value: Double, unit: DistanceUnit): String =
        "${round(value, 0)} ${unit.suffix}"

    fun cost(value: Double, currency: String): String =
        "$currency ${round(value, 2)}"

    fun pricePerUnit(value: Double, currency: String, unit: FuelUnit): String =
        "$currency ${round(value, 3)}/${unit.suffix}"

    fun percent(fraction: Double): String {
        val clamped = fraction.coerceIn(0.0, 1.0)
        return "${round(clamped * 100.0, 0)}%"
    }

    /**
     * Formats a consumption value using the vehicle's units. Returns null-safe
     * text; callers pass a non-null value only when a valid result exists.
     */
    fun consumption(value: Double, fuelUnit: FuelUnit, distanceUnit: DistanceUnit): String {
        return if (fuelUnit == FuelUnit.UsGallons && distanceUnit == DistanceUnit.Miles) {
            "${round(value, 1)} MPG"
        } else {
            "${round(value, 1)} L/100 km"
        }
    }

    fun consumptionUnitLabel(fuelUnit: FuelUnit, distanceUnit: DistanceUnit): String {
        return if (fuelUnit == FuelUnit.UsGallons && distanceUnit == DistanceUnit.Miles) {
            "MPG"
        } else {
            "L/100 km"
        }
    }

    fun costPerDistanceLabel(distanceUnit: DistanceUnit): String {
        return if (distanceUnit == DistanceUnit.Miles) "Cost per mile" else "Cost per 100 km"
    }
}
