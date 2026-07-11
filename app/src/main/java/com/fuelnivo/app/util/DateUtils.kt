package com.fuelnivo.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Date and time helpers built on java.time (enabled on minSdk 24 via core
 * library desugaring). Storage formats: date = yyyy-MM-dd, time = HH:mm.
 * All parsing is null-safe and never throws into the UI.
 */
object DateUtils {

    private val DATE_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val TIME_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val SHORT_DISPLAY: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d", Locale.US)
    private val LONG_DISPLAY: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

    fun today(): LocalDate = LocalDate.now()

    fun nowTimeString(): String = LocalTime.now().withSecond(0).withNano(0).format(TIME_FORMAT)

    fun todayString(): String = today().format(DATE_FORMAT)

    /** Parses a stored yyyy-MM-dd date, returning null if invalid. */
    fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value, DATE_FORMAT)
        } catch (e: Exception) {
            null
        }
    }

    /** Parses a stored HH:mm time, returning null if invalid. */
    fun parseTime(value: String?): LocalTime? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalTime.parse(value, TIME_FORMAT)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(date: LocalDate): String = date.format(DATE_FORMAT)

    fun formatTime(time: LocalTime): String = time.format(TIME_FORMAT)

    /** Short human label like "Jul 10"; falls back to the raw value. */
    fun shortDisplay(value: String?): String {
        val date = parseDate(value) ?: return value.orEmpty()
        return date.format(SHORT_DISPLAY)
    }

    /** Long human label like "Jul 10, 2026"; falls back to the raw value. */
    fun longDisplay(value: String?): String {
        val date = parseDate(value) ?: return value.orEmpty()
        return date.format(LONG_DISPLAY)
    }

    fun isValidDateString(value: String?): Boolean = parseDate(value) != null

    fun isValidTimeString(value: String?): Boolean =
        value.isNullOrBlank() || parseTime(value) != null

    fun yearMonthOf(value: String?): YearMonth? {
        val date = parseDate(value) ?: return null
        return YearMonth.from(date)
    }

    fun monthLabel(yearMonth: YearMonth): String {
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.US)
        return "$month ${yearMonth.year}"
    }

    /** Whole days between two stored dates, or null if either is invalid. */
    fun daysBetween(fromValue: String?, toDate: LocalDate): Long? {
        val from = parseDate(fromValue) ?: return null
        return java.time.temporal.ChronoUnit.DAYS.between(from, toDate)
    }
}
