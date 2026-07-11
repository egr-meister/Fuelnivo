package com.fuelnivo.app

import com.fuelnivo.app.util.Reminder
import com.fuelnivo.app.util.ReminderState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderTest {

    private val today = LocalDate.of(2026, 7, 10)

    private fun refills() = listOf(
        TestData.refill("r1", "2026-06-01", 1000.0, 30.0, fullTank = true)
    )

    @Test
    fun due_whenIntervalPassed() {
        val state = Reminder.evaluate(
            enabled = true, intervalDays = 14, hasActiveVehicle = true,
            vehicleRefills = refills(), today = today, dismissedThisSession = false
        )
        assertTrue(state is ReminderState.Due)
        assertEquals(39L, (state as ReminderState.Due).daysSince)
    }

    @Test
    fun notDue_whenIntervalNotReached() {
        val state = Reminder.evaluate(
            enabled = true, intervalDays = 90, hasActiveVehicle = true,
            vehicleRefills = refills(), today = today, dismissedThisSession = false
        )
        assertEquals(ReminderState.None, state)
    }

    @Test
    fun firstRecordPrompt_whenNoRefills() {
        val state = Reminder.evaluate(
            enabled = true, intervalDays = 14, hasActiveVehicle = true,
            vehicleRefills = emptyList(), today = today, dismissedThisSession = false
        )
        assertEquals(ReminderState.FirstRecordPrompt, state)
    }

    @Test
    fun none_whenDisabled() {
        val state = Reminder.evaluate(
            enabled = false, intervalDays = 14, hasActiveVehicle = true,
            vehicleRefills = refills(), today = today, dismissedThisSession = false
        )
        assertEquals(ReminderState.None, state)
    }

    @Test
    fun none_whenDismissed() {
        val state = Reminder.evaluate(
            enabled = true, intervalDays = 14, hasActiveVehicle = true,
            vehicleRefills = refills(), today = today, dismissedThisSession = true
        )
        assertEquals(ReminderState.None, state)
    }

    @Test
    fun none_whenNoActiveVehicle() {
        val state = Reminder.evaluate(
            enabled = true, intervalDays = 14, hasActiveVehicle = false,
            vehicleRefills = refills(), today = today, dismissedThisSession = false
        )
        assertEquals(ReminderState.None, state)
    }
}
