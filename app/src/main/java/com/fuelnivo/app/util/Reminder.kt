package com.fuelnivo.app.util

import com.fuelnivo.app.data.RefillRecord
import java.time.LocalDate

/** State of the in-app refill reminder for the active vehicle. */
sealed interface ReminderState {
    /** Reminder disabled, dismissed, or not yet due. */
    data object None : ReminderState

    /** No refill has ever been recorded for the active vehicle. */
    data object FirstRecordPrompt : ReminderState

    /** A refill is due; [daysSince] days have passed since the last one. */
    data class Due(val daysSince: Long, val intervalDays: Int) : ReminderState
}

/**
 * Fully local, evaluated on demand (no alarms, services, or notifications).
 */
object Reminder {

    /**
     * Determines whether a reminder is due for the active vehicle.
     *
     * @param enabled whether reminders are enabled in settings
     * @param intervalDays configured reminder interval
     * @param vehicleRefills the active vehicle's refills (may be empty)
     * @param today the current local date
     * @param dismissedThisSession whether the user dismissed it this session
     */
    fun evaluate(
        enabled: Boolean,
        intervalDays: Int,
        hasActiveVehicle: Boolean,
        vehicleRefills: List<RefillRecord>,
        today: LocalDate,
        dismissedThisSession: Boolean
    ): ReminderState {
        if (!enabled || !hasActiveVehicle || dismissedThisSession) return ReminderState.None

        val latestDate = vehicleRefills
            .mapNotNull { DateUtils.parseDate(it.date) }
            .maxOrNull()
            ?: return ReminderState.FirstRecordPrompt

        val safeInterval = intervalDays.coerceIn(1, 90)
        val daysSince = java.time.temporal.ChronoUnit.DAYS.between(latestDate, today)
        return if (daysSince >= safeInterval) {
            ReminderState.Due(daysSince = daysSince, intervalDays = safeInterval)
        } else {
            ReminderState.None
        }
    }
}
