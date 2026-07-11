package com.fuelnivo.app

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.FuelUnit
import com.fuelnivo.app.util.FuelCalculations
import com.fuelnivo.app.util.IntervalReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FuelCalculationsTest {

    private val delta = 1e-6

    private fun standardRefills() = listOf(
        TestData.refill("r1", "2026-01-01", 1000.0, 40.0, fullTank = true),
        TestData.refill("r2", "2026-01-10", 1500.0, 30.0, fullTank = true),
        TestData.refill("r3", "2026-01-15", 1700.0, 10.0, fullTank = false),
        TestData.refill("r4", "2026-01-20", 2000.0, 25.0, fullTank = true)
    )

    @Test
    fun consumptionValue_dividesByZero_returnsNull() {
        assertNull(FuelCalculations.consumptionValue(10.0, 0.0, FuelUnit.Liters, DistanceUnit.Kilometers))
    }

    @Test
    fun consumptionValue_negativeDistance_returnsNull() {
        assertNull(FuelCalculations.consumptionValue(10.0, -5.0, FuelUnit.Liters, DistanceUnit.Kilometers))
    }

    @Test
    fun consumptionValue_zeroFuel_returnsNull() {
        assertNull(FuelCalculations.consumptionValue(0.0, 100.0, FuelUnit.Liters, DistanceUnit.Kilometers))
    }

    @Test
    fun l100km_simpleInterval_isCorrect() {
        val vehicle = TestData.vehicle()
        val asc = FuelCalculations.sortedAscending(standardRefills())
        val interval = FuelCalculations.intervalEndingAt(vehicle, asc, asc.first { it.id == "r2" })
        assertEquals(IntervalReason.VALID, interval.reason)
        assertEquals(6.0, interval.consumption!!, delta) // 30 / 500 * 100
    }

    @Test
    fun l100km_withPartialRefill_aggregatesFuel() {
        val vehicle = TestData.vehicle()
        val asc = FuelCalculations.sortedAscending(standardRefills())
        val interval = FuelCalculations.intervalEndingAt(vehicle, asc, asc.first { it.id == "r4" })
        // (10 + 25) / 500 * 100 = 7.0
        assertEquals(7.0, interval.consumption!!, delta)
        assertEquals(35.0, interval.fuelUsed!!, delta)
    }

    @Test
    fun latestConsumption_usesMostRecentValidInterval() {
        val vehicle = TestData.vehicle()
        assertEquals(7.0, FuelCalculations.latestConsumption(vehicle, standardRefills())!!, delta)
    }

    @Test
    fun lifetimeConsumption_aggregatesAllValidIntervals() {
        val vehicle = TestData.vehicle()
        // total fuel 30 + 35 = 65, total distance 1000 => 6.5
        assertEquals(6.5, FuelCalculations.lifetimeConsumption(vehicle, standardRefills())!!, delta)
    }

    @Test
    fun missedRefill_excludesInterval() {
        val vehicle = TestData.vehicle()
        val refills = listOf(
            TestData.refill("r1", "2026-01-01", 1000.0, 40.0, fullTank = true),
            TestData.refill("r2", "2026-01-10", 1500.0, 30.0, fullTank = true),
            TestData.refill("r4", "2026-01-20", 2000.0, 25.0, fullTank = true, missed = true)
        )
        val asc = FuelCalculations.sortedAscending(refills)
        val interval = FuelCalculations.intervalEndingAt(vehicle, asc, asc.first { it.id == "r4" })
        assertEquals(IntervalReason.MISSED_REFILL, interval.reason)
        // Latest valid falls back to r2 interval (6.0)
        assertEquals(6.0, FuelCalculations.latestConsumption(vehicle, refills)!!, delta)
    }

    @Test
    fun singleFullTank_hasNoPreviousInterval() {
        val vehicle = TestData.vehicle()
        val refills = listOf(TestData.refill("r1", "2026-01-01", 1000.0, 40.0, fullTank = true))
        assertNull(FuelCalculations.latestConsumption(vehicle, refills))
        val asc = FuelCalculations.sortedAscending(refills)
        val interval = FuelCalculations.intervalEndingAt(vehicle, asc, refills.first())
        assertEquals(IntervalReason.NO_PREVIOUS_FULL_TANK, interval.reason)
    }

    @Test
    fun partialTargetRefill_isNotFullTankReason() {
        val vehicle = TestData.vehicle()
        val asc = FuelCalculations.sortedAscending(standardRefills())
        val interval = FuelCalculations.intervalEndingAt(vehicle, asc, asc.first { it.id == "r3" })
        assertEquals(IntervalReason.NOT_FULL_TANK, interval.reason)
    }

    @Test
    fun mpg_isCalculatedForMilesAndGallons() {
        val vehicle = TestData.vehicle(fuelUnit = FuelUnit.UsGallons, distanceUnit = DistanceUnit.Miles)
        val refills = listOf(
            TestData.refill("r1", "2026-01-01", 1000.0, 10.0, fullTank = true),
            TestData.refill("r2", "2026-01-10", 1300.0, 10.0, fullTank = true) // 300 mi / 10 gal = 30 MPG
        )
        assertEquals(30.0, FuelCalculations.latestConsumption(vehicle, refills)!!, delta)
    }

    @Test
    fun outOfOrderOdometer_yieldsNonPositiveDistance() {
        val vehicle = TestData.vehicle()
        val refills = listOf(
            TestData.refill("r1", "2026-01-01", 2000.0, 30.0, fullTank = true),
            TestData.refill("r2", "2026-01-10", 1500.0, 30.0, fullTank = true)
        )
        val asc = FuelCalculations.sortedAscending(refills)
        val interval = FuelCalculations.intervalEndingAt(vehicle, asc, asc.first { it.id == "r2" })
        assertEquals(IntervalReason.NON_POSITIVE_DISTANCE, interval.reason)
        assertNull(FuelCalculations.latestConsumption(vehicle, refills))
    }

    @Test
    fun emptyRefills_returnNullResults() {
        val vehicle = TestData.vehicle()
        assertNull(FuelCalculations.latestConsumption(vehicle, emptyList()))
        assertNull(FuelCalculations.lifetimeConsumption(vehicle, emptyList()))
        assertNull(FuelCalculations.currentOdometer(emptyList()))
        assertTrue(FuelCalculations.allIntervals(vehicle, emptyList()).isEmpty())
    }

    @Test
    fun longestInterval_isCorrect() {
        assertEquals(10L, FuelCalculations.longestIntervalDays(standardRefills()))
    }
}
