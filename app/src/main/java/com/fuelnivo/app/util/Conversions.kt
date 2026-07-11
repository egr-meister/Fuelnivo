package com.fuelnivo.app.util

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.FuelUnit

/**
 * Pure unit-conversion helpers. All functions use Double internally and never
 * throw. Formatting happens only at the UI layer.
 */
object Conversions {

    const val LITERS_PER_US_GALLON = 3.785411784
    const val KILOMETERS_PER_MILE = 1.609344

    fun litersToGallons(liters: Double): Double = liters / LITERS_PER_US_GALLON

    fun gallonsToLiters(gallons: Double): Double = gallons * LITERS_PER_US_GALLON

    fun kilometersToMiles(km: Double): Double = km / KILOMETERS_PER_MILE

    fun milesToKilometers(miles: Double): Double = miles * KILOMETERS_PER_MILE

    /** Converts a fuel volume from one unit to another. */
    fun convertFuel(value: Double, from: FuelUnit, to: FuelUnit): Double {
        if (from == to) return value
        return when {
            from == FuelUnit.Liters && to == FuelUnit.UsGallons -> litersToGallons(value)
            from == FuelUnit.UsGallons && to == FuelUnit.Liters -> gallonsToLiters(value)
            else -> value
        }
    }

    /** Converts a distance/odometer value from one unit to another. */
    fun convertDistance(value: Double, from: DistanceUnit, to: DistanceUnit): Double {
        if (from == to) return value
        return when {
            from == DistanceUnit.Kilometers && to == DistanceUnit.Miles -> kilometersToMiles(value)
            from == DistanceUnit.Miles && to == DistanceUnit.Kilometers -> milesToKilometers(value)
            else -> value
        }
    }

    /**
     * Converts a price-per-unit value when the fuel unit changes. The price is
     * expressed per volume unit, so it scales inversely to the volume
     * conversion (e.g. price per liter -> price per gallon multiplies by
     * liters-per-gallon).
     */
    fun convertPricePerUnit(price: Double, from: FuelUnit, to: FuelUnit): Double {
        if (from == to) return price
        return when {
            from == FuelUnit.Liters && to == FuelUnit.UsGallons -> price * LITERS_PER_US_GALLON
            from == FuelUnit.UsGallons && to == FuelUnit.Liters -> price / LITERS_PER_US_GALLON
            else -> price
        }
    }
}
