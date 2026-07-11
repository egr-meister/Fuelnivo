package com.fuelnivo.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fuel type of a vehicle. */
@Serializable
enum class FuelType(val label: String) {
    @SerialName("Gasoline") Gasoline("Gasoline"),
    @SerialName("Diesel") Diesel("Diesel"),
    @SerialName("LPG") LPG("LPG"),
    @SerialName("Hybrid") Hybrid("Hybrid"),
    @SerialName("Other") Other("Other");

    companion object {
        fun fromNameOrDefault(name: String?): FuelType =
            entries.firstOrNull { it.name == name } ?: Gasoline
    }
}

/** Volume unit used to record fuel amounts. */
@Serializable
enum class FuelUnit(val label: String, val suffix: String) {
    @SerialName("Liters") Liters("Liters", "L"),
    @SerialName("UsGallons") UsGallons("US Gallons", "gal");

    companion object {
        fun fromNameOrDefault(name: String?): FuelUnit =
            entries.firstOrNull { it.name == name } ?: Liters
    }
}

/** Distance unit used for odometer and distance values. */
@Serializable
enum class DistanceUnit(val label: String, val suffix: String) {
    @SerialName("Kilometers") Kilometers("Kilometers", "km"),
    @SerialName("Miles") Miles("Miles", "mi");

    companion object {
        fun fromNameOrDefault(name: String?): DistanceUnit =
            entries.firstOrNull { it.name == name } ?: Kilometers
    }
}

/**
 * A user vehicle. All fields are manually entered. Defaults are provided so
 * older stored JSON that is missing newly added fields still deserializes.
 */
@Serializable
data class Vehicle(
    val id: String = "",
    val name: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val year: Int? = null,
    val fuelType: FuelType = FuelType.Gasoline,
    val tankCapacity: Double = 0.0,
    val fuelUnit: FuelUnit = FuelUnit.Liters,
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometers,
    val initialOdometer: Double? = null,
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * A single manual refill record for a vehicle. Numeric values are stored as
 * numbers, never as formatted strings. Optional monetary fields are nullable.
 */
@Serializable
data class RefillRecord(
    val id: String = "",
    val vehicleId: String = "",
    val date: String = "",
    val time: String = "",
    val odometer: Double = 0.0,
    val fuelAmount: Double = 0.0,
    val totalCost: Double? = null,
    val pricePerUnit: Double? = null,
    val fullTank: Boolean = false,
    val missedPreviousRefill: Boolean = false,
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

/** In-app reminder configuration. */
@Serializable
data class ReminderSettings(
    val enabled: Boolean = true,
    val intervalDays: Int = 14
)

/** Application-wide settings. */
@Serializable
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val activeVehicleId: String? = null,
    val currencyCode: String = "USD",
    val reminderSettings: ReminderSettings = ReminderSettings()
)

/** Aggregate root persisted to DataStore. */
@Serializable
data class AppData(
    val vehicles: List<Vehicle> = emptyList(),
    val refills: List<RefillRecord> = emptyList(),
    val settings: AppSettings = AppSettings()
)
