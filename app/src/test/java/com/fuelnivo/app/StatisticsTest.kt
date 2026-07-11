package com.fuelnivo.app

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.util.Statistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class StatisticsTest {

    private val delta = 1e-6

    private fun refills() = listOf(
        TestData.refill("r1", "2026-01-01", 1000.0, 40.0, fullTank = true, cost = 60.0),
        TestData.refill("r2", "2026-01-10", 1500.0, 30.0, fullTank = true, cost = 45.0),
        TestData.refill("r3", "2026-02-05", 2000.0, 25.0, fullTank = true, cost = 40.0)
    )

    @Test
    fun monthlyTotals_fuelAndCost_areCorrect() {
        val vehicle = TestData.vehicle()
        val jan = Statistics.monthlySummary(vehicle, refills(), YearMonth.of(2026, 1))
        assertEquals(2, jan.refillCount)
        assertEquals(70.0, jan.totalFuel, delta)
        assertEquals(105.0, jan.totalCost!!, delta)
        assertEquals(35.0, jan.averageRefillAmount!!, delta)
        assertEquals(52.5, jan.averageRefillCost!!, delta)
    }

    @Test
    fun monthlySummary_emptyMonth_hasNoData() {
        val vehicle = TestData.vehicle()
        val march = Statistics.monthlySummary(vehicle, refills(), YearMonth.of(2026, 3))
        assertFalse(march.hasData)
        assertNull(march.totalCost)
    }

    @Test
    fun lifetimeStats_costAndFuel_areCorrect() {
        val vehicle = TestData.vehicle()
        val stats = Statistics.lifetimeStats(vehicle, refills())
        assertEquals(3, stats.totalRefills)
        assertEquals(95.0, stats.totalFuel, delta)
        assertEquals(145.0, stats.totalCost!!, delta)
        assertEquals(1000.0, stats.totalDistance!!, delta) // 2000 - 1000
    }

    @Test
    fun costPer100km_isCorrect() {
        val vehicle = TestData.vehicle(distanceUnit = DistanceUnit.Kilometers)
        val stats = Statistics.lifetimeStats(vehicle, refills())
        // total cost 145 over 1000 km => 14.5 per 100 km
        assertEquals(14.5, stats.costPerDistanceUnit!!, delta)
    }

    @Test
    fun derivePricePerUnit_guardsZeroFuel() {
        assertNull(Statistics.derivePricePerUnit(50.0, 0.0))
        assertEquals(2.0, Statistics.derivePricePerUnit(50.0, 25.0)!!, delta)
    }

    @Test
    fun deriveTotalCost_multipliesSafely() {
        assertEquals(50.0, Statistics.deriveTotalCost(25.0, 2.0)!!, delta)
        assertNull(Statistics.deriveTotalCost(null, 2.0))
    }

    @Test
    fun emptyRefills_lifetimeStats_areSafe() {
        val vehicle = TestData.vehicle()
        val stats = Statistics.lifetimeStats(vehicle, emptyList())
        assertEquals(0, stats.totalRefills)
        assertNull(stats.totalCost)
        assertNull(stats.totalDistance)
    }
}
