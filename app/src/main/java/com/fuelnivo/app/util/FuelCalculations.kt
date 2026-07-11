package com.fuelnivo.app.util

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.FuelUnit
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.data.Vehicle
import java.time.YearMonth

/** Reason a consumption interval could not be calculated. */
enum class IntervalReason {
    VALID,
    NOT_FULL_TANK,
    NO_PREVIOUS_FULL_TANK,
    MISSED_REFILL,
    NON_POSITIVE_DISTANCE
}

/** Result of a full-tank consumption interval that ends at a given refill. */
data class ConsumptionInterval(
    val endRefillId: String,
    val reason: IntervalReason,
    val distance: Double? = null,
    val fuelUsed: Double? = null,
    val consumption: Double? = null
) {
    val isValid: Boolean get() = reason == IntervalReason.VALID && consumption != null
}

/**
 * Pure, null-safe fuel calculations built on the full-tank method. Nothing
 * here throws; insufficient data yields null results and explicit reasons.
 */
object FuelCalculations {

    /** Refills for a vehicle sorted chronologically (oldest first). */
    fun sortedAscending(refills: List<RefillRecord>): List<RefillRecord> =
        refills.sortedWith(
            compareBy(
                { DateUtils.parseDate(it.date) ?: java.time.LocalDate.MIN },
                { it.time },
                { it.odometer },
                { it.createdAt }
            )
        )

    fun sortedDescending(refills: List<RefillRecord>): List<RefillRecord> =
        sortedAscending(refills).reversed()

    /** Raw consumption value in the vehicle's native display units. */
    fun consumptionValue(
        fuelUsed: Double,
        distance: Double,
        fuelUnit: FuelUnit,
        distanceUnit: DistanceUnit
    ): Double? {
        if (distance <= 0.0 || fuelUsed <= 0.0) return null
        return if (fuelUnit == FuelUnit.UsGallons && distanceUnit == DistanceUnit.Miles) {
            distance / fuelUsed
        } else {
            fuelUsed / distance * 100.0
        }
    }

    /**
     * Computes the consumption interval that ends at [target], using the most
     * recent previous full-tank refill as the start. Partial refills between
     * the two full-tank records are added to the consumed fuel.
     */
    fun intervalEndingAt(
        vehicle: Vehicle,
        refillsAsc: List<RefillRecord>,
        target: RefillRecord
    ): ConsumptionInterval {
        if (!target.fullTank) {
            return ConsumptionInterval(target.id, IntervalReason.NOT_FULL_TANK)
        }
        val targetIndex = refillsAsc.indexOfFirst { it.id == target.id }
        if (targetIndex < 0) {
            return ConsumptionInterval(target.id, IntervalReason.NO_PREVIOUS_FULL_TANK)
        }
        // Find previous full-tank refill.
        var startIndex = -1
        for (i in targetIndex - 1 downTo 0) {
            if (refillsAsc[i].fullTank) {
                startIndex = i
                break
            }
        }
        if (startIndex < 0) {
            return ConsumptionInterval(target.id, IntervalReason.NO_PREVIOUS_FULL_TANK)
        }
        val start = refillsAsc[startIndex]

        // Refills that fall within (start, target]. A missed marker on any of
        // them invalidates the interval.
        val intervalRefills = refillsAsc.subList(startIndex + 1, targetIndex + 1)
        if (intervalRefills.any { it.missedPreviousRefill }) {
            return ConsumptionInterval(target.id, IntervalReason.MISSED_REFILL)
        }

        val distance = target.odometer - start.odometer
        if (distance <= 0.0) {
            return ConsumptionInterval(target.id, IntervalReason.NON_POSITIVE_DISTANCE, distance = distance)
        }

        val fuelUsed = intervalRefills.sumOf { it.fuelAmount }
        val consumption = consumptionValue(fuelUsed, distance, vehicle.fuelUnit, vehicle.distanceUnit)
            ?: return ConsumptionInterval(target.id, IntervalReason.NON_POSITIVE_DISTANCE, distance = distance, fuelUsed = fuelUsed)

        return ConsumptionInterval(
            endRefillId = target.id,
            reason = IntervalReason.VALID,
            distance = distance,
            fuelUsed = fuelUsed,
            consumption = consumption
        )
    }

    /** All valid + invalid intervals across a vehicle's full-tank refills. */
    fun allIntervals(vehicle: Vehicle, refills: List<RefillRecord>): List<ConsumptionInterval> {
        val asc = sortedAscending(refills)
        return asc.filter { it.fullTank }.map { intervalEndingAt(vehicle, asc, it) }
    }

    fun validIntervals(vehicle: Vehicle, refills: List<RefillRecord>): List<ConsumptionInterval> =
        allIntervals(vehicle, refills).filter { it.isValid }

    /** Latest valid interval consumption, or null when none exists. */
    fun latestConsumption(vehicle: Vehicle, refills: List<RefillRecord>): Double? {
        val asc = sortedAscending(refills)
        for (refill in asc.filter { it.fullTank }.reversed()) {
            val interval = intervalEndingAt(vehicle, asc, refill)
            if (interval.isValid) return interval.consumption
        }
        return null
    }

    /** Lifetime average consumption aggregated over all valid intervals. */
    fun lifetimeConsumption(vehicle: Vehicle, refills: List<RefillRecord>): Double? {
        val valid = validIntervals(vehicle, refills)
        if (valid.isEmpty()) return null
        val totalFuel = valid.sumOf { it.fuelUsed ?: 0.0 }
        val totalDistance = valid.sumOf { it.distance ?: 0.0 }
        return consumptionValue(totalFuel, totalDistance, vehicle.fuelUnit, vehicle.distanceUnit)
    }

    /** Average consumption for valid intervals ending within [month]. */
    fun monthlyConsumption(
        vehicle: Vehicle,
        refills: List<RefillRecord>,
        month: YearMonth
    ): Double? {
        val asc = sortedAscending(refills)
        val valid = asc.filter { it.fullTank }
            .map { it to intervalEndingAt(vehicle, asc, it) }
            .filter { (refill, interval) ->
                interval.isValid && DateUtils.yearMonthOf(refill.date) == month
            }
        if (valid.isEmpty()) return null
        val totalFuel = valid.sumOf { it.second.fuelUsed ?: 0.0 }
        val totalDistance = valid.sumOf { it.second.distance ?: 0.0 }
        return consumptionValue(totalFuel, totalDistance, vehicle.fuelUnit, vehicle.distanceUnit)
    }

    /** Distance since the immediately previous refill (any type), or null. */
    fun distanceSincePrevious(refillsAsc: List<RefillRecord>, target: RefillRecord): Double? {
        val index = refillsAsc.indexOfFirst { it.id == target.id }
        if (index <= 0) return null
        val distance = target.odometer - refillsAsc[index - 1].odometer
        return if (distance > 0.0) distance else null
    }

    /** Latest recorded odometer (most recent refill), or null. */
    fun currentOdometer(refills: List<RefillRecord>): Double? =
        sortedDescending(refills).firstOrNull()?.odometer

    fun firstOdometer(vehicle: Vehicle, refills: List<RefillRecord>): Double? {
        val firstRefill = sortedAscending(refills).firstOrNull()?.odometer
        return firstRefill ?: vehicle.initialOdometer
    }

    /** Total recorded distance between first and last refill odometer. */
    fun totalDistance(vehicle: Vehicle, refills: List<RefillRecord>): Double? {
        val asc = sortedAscending(refills)
        if (asc.size < 2) return null
        val distance = asc.last().odometer - asc.first().odometer
        return if (distance > 0.0) distance else null
    }

    /** Longest gap (in days) between consecutive refills, or null. */
    fun longestIntervalDays(refills: List<RefillRecord>): Long? {
        val dates = sortedAscending(refills).mapNotNull { DateUtils.parseDate(it.date) }
        if (dates.size < 2) return null
        var longest = 0L
        for (i in 1 until dates.size) {
            val gap = java.time.temporal.ChronoUnit.DAYS.between(dates[i - 1], dates[i])
            if (gap > longest) longest = gap
        }
        return longest
    }
}
