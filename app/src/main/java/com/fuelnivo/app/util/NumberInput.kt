package com.fuelnivo.app.util

import java.util.Locale

/**
 * Safe numeric input parsing. Normalizes a comma decimal separator to a point
 * and tolerates blank/partial input while typing without throwing.
 */
object NumberInput {

    /** Normalizes user text: trims and converts a single comma to a point. */
    fun normalize(raw: String): String = raw.trim().replace(',', '.')

    /** Parses to Double, or null when blank/invalid. */
    fun parseOrNull(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        val normalized = normalize(raw)
        if (normalized == "." || normalized == "-" || normalized.isBlank()) return null
        return normalized.toDoubleOrNull()
    }

    /** Parses to Int, or null when blank/invalid. */
    fun parseIntOrNull(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        return normalize(raw).toIntOrNull()
    }

    /** True while the text is a valid intermediate for decimal entry. */
    fun isEditableDecimal(raw: String): Boolean {
        if (raw.isEmpty()) return true
        val normalized = normalize(raw)
        return normalized.matches(Regex("^\\d*\\.?\\d*$"))
    }

    fun formatPlain(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)
}
