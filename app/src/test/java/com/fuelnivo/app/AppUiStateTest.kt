package com.fuelnivo.app

import com.fuelnivo.app.data.AppData
import com.fuelnivo.app.data.AppSettings
import com.fuelnivo.app.ui.AppUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppUiStateTest {

    private val v1 = TestData.vehicle(id = "v1")
    private val v2 = TestData.vehicle(id = "v2")

    @Test
    fun activeVehicle_fallsBackToFirst_whenIdMissing() {
        val state = AppUiState(
            loaded = true,
            data = AppData(
                vehicles = listOf(v1, v2),
                refills = emptyList(),
                settings = AppSettings(activeVehicleId = "does-not-exist")
            )
        )
        assertEquals("v1", state.activeVehicle?.id)
    }

    @Test
    fun activeVehicle_fallsBackToFirst_whenIdNull() {
        val state = AppUiState(
            loaded = true,
            data = AppData(vehicles = listOf(v1, v2), settings = AppSettings(activeVehicleId = null))
        )
        assertEquals("v1", state.activeVehicle?.id)
    }

    @Test
    fun activeVehicle_isNull_whenNoVehicles() {
        val state = AppUiState(loaded = true, data = AppData())
        assertNull(state.activeVehicle)
    }

    @Test
    fun refillsForVehicle_filtersByVehicleId() {
        val state = AppUiState(
            loaded = true,
            data = AppData(
                vehicles = listOf(v1, v2),
                refills = listOf(
                    TestData.refill("r1", "2026-01-01", 100.0, 10.0, true, vehicleId = "v1"),
                    TestData.refill("r2", "2026-01-02", 200.0, 10.0, true, vehicleId = "v2")
                )
            )
        )
        assertEquals(1, state.refillsForVehicle("v1").size)
        assertEquals("r1", state.refillsForVehicle("v1").first().id)
    }
}
