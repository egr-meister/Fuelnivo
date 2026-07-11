package com.fuelnivo.app

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.FuelType
import com.fuelnivo.app.data.FuelUnit
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.data.Vehicle

/** Shared builders for calculation tests. */
object TestData {

    fun vehicle(
        id: String = "v1",
        fuelUnit: FuelUnit = FuelUnit.Liters,
        distanceUnit: DistanceUnit = DistanceUnit.Kilometers,
        capacity: Double = 55.0
    ) = Vehicle(
        id = id,
        name = "Test Car",
        manufacturer = "",
        model = "",
        year = null,
        fuelType = FuelType.Gasoline,
        tankCapacity = capacity,
        fuelUnit = fuelUnit,
        distanceUnit = distanceUnit,
        initialOdometer = null,
        note = "",
        createdAt = "2026-01-01",
        updatedAt = "2026-01-01"
    )

    fun refill(
        id: String,
        date: String,
        odometer: Double,
        fuel: Double,
        fullTank: Boolean,
        cost: Double? = null,
        missed: Boolean = false,
        vehicleId: String = "v1"
    ) = RefillRecord(
        id = id,
        vehicleId = vehicleId,
        date = date,
        time = "12:00",
        odometer = odometer,
        fuelAmount = fuel,
        totalCost = cost,
        pricePerUnit = null,
        fullTank = fullTank,
        missedPreviousRefill = missed,
        note = "",
        createdAt = date,
        updatedAt = date
    )
}
