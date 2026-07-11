package com.fuelnivo.app

import com.fuelnivo.app.data.DistanceUnit
import com.fuelnivo.app.data.FuelUnit
import com.fuelnivo.app.util.Conversions
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversionsTest {

    private val delta = 1e-6

    @Test
    fun litersToGallons_isCorrect() {
        assertEquals(1.0, Conversions.litersToGallons(3.785411784), delta)
    }

    @Test
    fun gallonsToLiters_isCorrect() {
        assertEquals(3.785411784, Conversions.gallonsToLiters(1.0), delta)
    }

    @Test
    fun kilometersToMiles_isCorrect() {
        assertEquals(1.0, Conversions.kilometersToMiles(1.609344), delta)
    }

    @Test
    fun milesToKilometers_isCorrect() {
        assertEquals(1.609344, Conversions.milesToKilometers(1.0), delta)
    }

    @Test
    fun convertFuel_roundTrip_preservesValue() {
        val original = 42.5
        val toGal = Conversions.convertFuel(original, FuelUnit.Liters, FuelUnit.UsGallons)
        val back = Conversions.convertFuel(toGal, FuelUnit.UsGallons, FuelUnit.Liters)
        assertEquals(original, back, delta)
    }

    @Test
    fun convertDistance_roundTrip_preservesValue() {
        val original = 12345.0
        val toMi = Conversions.convertDistance(original, DistanceUnit.Kilometers, DistanceUnit.Miles)
        val back = Conversions.convertDistance(toMi, DistanceUnit.Miles, DistanceUnit.Kilometers)
        assertEquals(original, back, 1e-4)
    }

    @Test
    fun convertPricePerUnit_scalesInversely() {
        // 2.00 per liter -> per gallon should be ~7.57
        val perGallon = Conversions.convertPricePerUnit(2.0, FuelUnit.Liters, FuelUnit.UsGallons)
        assertEquals(2.0 * 3.785411784, perGallon, delta)
    }

    @Test
    fun sameUnit_returnsInput() {
        assertEquals(5.0, Conversions.convertFuel(5.0, FuelUnit.Liters, FuelUnit.Liters), delta)
    }
}
