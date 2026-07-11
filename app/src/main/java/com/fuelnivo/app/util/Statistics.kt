package com.fuelnivo.app.util

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.data.Vehicle
import java.time.YearMonth

/** Aggregated values for a single calendar month. */
data class MonthlySummary(
    val month: YearMonth,
    val refillCount: Int = 0,
    val totalFuel: Double = 0.0,
    val totalCost: Double? = null,
    val totalDistance: Double? = null,
    val averageRefillAmount: Double? = null,
    val averageRefillCost: Double? = null,
    val averageConsumption: Double? = null,
    val highestRefill: Double? = null,
    val longestIntervalDays: Long? = null
) {
    val hasData: Boolean get() = refillCount > 0
}

/** Lifetime statistics for a vehicle. */
data class LifetimeStats(
    val totalRefills: Int = 0,
    val totalFuel: Double = 0.0,
    val totalCost: Double? = null,
    val currentOdometer: Double? = null,
    val firstOdometer: Double? = null,
    val totalDistance: Double? = null,
    val latestConsumption: Double? = null,
    val lifetimeConsumption: Double? = null,
    val averageRefillAmount: Double? = null,
    val averageRefillCost: Double? = null,
    val fullTankCount: Int = 0,
    val excludedIntervalCount: Int = 0,
    val validIntervalCount: Int = 0,
    val costPerDistanceUnit: Double? = null
)

/**
 * Monthly and lifetime statistics + cost calculations. All results are
 * null-safe: missing inputs produce null rather than misleading zeros.
 */
object Statistics {

    private fun refillsInMonth(refills: List<RefillRecord>, month: YearMonth): List<RefillRecord> =
        refills.filter { DateUtils.yearMonthOf(it.date) == month }

    private fun costsOf(refills: List<RefillRecord>): List<Double> =
        refills.mapNotNull { it.totalCost }.filter { it >= 0.0 }

    /** Sum of positive distance-since-previous for the given refills. */
    private fun distanceForRefills(
        allRefillsAsc: List<RefillRecord>,
        subset: List<RefillRecord>
    ): Double? {
        var total = 0.0
        var found = false
        for (refill in subset) {
            val d = FuelCalculations.distanceSincePrevious(allRefillsAsc, refill)
            if (d != null) {
                total += d
                found = true
            }
        }
        return if (found) total else null
    }

    fun monthlySummary(
        vehicle: Vehicle,
        refills: List<RefillRecord>,
        month: YearMonth
    ): MonthlySummary {
        val asc = FuelCalculations.sortedAscending(refills)
        val monthRefills = refillsInMonth(asc, month)
        if (monthRefills.isEmpty()) return MonthlySummary(month = month)

        val totalFuel = monthRefills.sumOf { it.fuelAmount }
        val costs = costsOf(monthRefills)
        val totalCost = if (costs.isEmpty()) null else costs.sum()
        val avgCost = if (costs.isEmpty()) null else costs.average()
        val distance = distanceForRefills(asc, monthRefills)

        return MonthlySummary(
            month = month,
            refillCount = monthRefills.size,
            totalFuel = totalFuel,
            totalCost = totalCost,
            totalDistance = distance,
            averageRefillAmount = totalFuel / monthRefills.size,
            averageRefillCost = avgCost,
            averageConsumption = FuelCalculations.monthlyConsumption(vehicle, refills, month),
            highestRefill = monthRefills.maxOf { it.fuelAmount },
            longestIntervalDays = FuelCalculations.longestIntervalDays(monthRefills)
        )
    }

    fun lifetimeStats(vehicle: Vehicle, refills: List<RefillRecord>): LifetimeStats {
        if (refills.isEmpty()) return LifetimeStats()

        val totalFuel = refills.sumOf { it.fuelAmount }
        val costs = costsOf(refills)
        val totalCost = if (costs.isEmpty()) null else costs.sum()
        val intervals = FuelCalculations.allIntervals(vehicle, refills)
        val valid = intervals.filter { it.isValid }
        val excluded = intervals.count { !it.isValid }
        val totalDistance = FuelCalculations.totalDistance(vehicle, refills)

        val costPerDistance = if (totalCost != null && totalDistance != null && totalDistance > 0.0) {
            if (vehicle.distanceUnit == DistanceUnit.Miles) {
                totalCost / totalDistance
            } else {
                totalCost / totalDistance * 100.0
            }
        } else null

        return LifetimeStats(
            totalRefills = refills.size,
            totalFuel = totalFuel,
            totalCost = totalCost,
            currentOdometer = FuelCalculations.currentOdometer(refills),
            firstOdometer = FuelCalculations.firstOdometer(vehicle, refills),
            totalDistance = totalDistance,
            latestConsumption = FuelCalculations.latestConsumption(vehicle, refills),
            lifetimeConsumption = FuelCalculations.lifetimeConsumption(vehicle, refills),
            averageRefillAmount = totalFuel / refills.size,
            averageRefillCost = if (costs.isEmpty()) null else costs.average(),
            fullTankCount = refills.count { it.fullTank },
            excludedIntervalCount = excluded,
            validIntervalCount = valid.size,
            costPerDistanceUnit = costPerDistance
        )
    }

    /** Price per unit derived from cost and amount, if both are usable. */
    fun derivePricePerUnit(totalCost: Double?, fuelAmount: Double?): Double? {
        if (totalCost == null || fuelAmount == null) return null
        if (fuelAmount <= 0.0 || totalCost < 0.0) return null
        return totalCost / fuelAmount
    }

    /** Total cost derived from amount and price per unit, if both are usable. */
    fun deriveTotalCost(fuelAmount: Double?, pricePerUnit: Double?): Double? {
        if (fuelAmount == null || pricePerUnit == null) return null
        if (fuelAmount < 0.0 || pricePerUnit < 0.0) return null
        return fuelAmount * pricePerUnit
    }
}
