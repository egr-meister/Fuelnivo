package com.fuelnivo.app

import com.fuelnivo.app.data.AppSettings
import com.fuelnivo.app.data.Vehicle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mirrors the repository's tolerant JSON configuration to verify that missing
 * fields and unknown keys deserialize into safe defaults, and that malformed
 * JSON is handled by the caller (never returned as partial data).
 */
class ModelSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @Test
    fun missingFields_useDefaults() {
        val raw = """{"id":"v1","name":"Car","tankCapacity":50.0}"""
        val vehicle = json.decodeFromString<Vehicle>(raw)
        assertEquals("v1", vehicle.id)
        assertEquals(50.0, vehicle.tankCapacity, 1e-9)
        assertEquals("", vehicle.manufacturer) // default
    }

    @Test
    fun unknownKeys_areIgnored() {
        val raw = """{"currencyCode":"EUR","futureField":"ignored"}"""
        val settings = json.decodeFromString<AppSettings>(raw)
        assertEquals("EUR", settings.currencyCode)
        assertEquals(14, settings.reminderSettings.intervalDays) // default
    }

    @Test
    fun corruptedJson_throwsAndIsCaughtByCaller() {
        val raw = "{not valid json"
        val result = runCatching { json.decodeFromString<Vehicle>(raw) }
        assertTrue(result.isFailure)
    }

    @Test
    fun roundTrip_isStable() {
        val original = Vehicle(id = "v9", name = "Blue Hatchback", tankCapacity = 42.0)
        val encoded = json.encodeToString(Vehicle.serializer(), original)
        val decoded = json.decodeFromString<Vehicle>(encoded)
        assertEquals(original, decoded)
    }
}
