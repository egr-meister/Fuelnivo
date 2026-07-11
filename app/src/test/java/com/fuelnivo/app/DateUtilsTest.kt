package com.fuelnivo.app

import com.fuelnivo.app.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun validDate_parses() {
        assertTrue(DateUtils.isValidDateString("2026-07-10"))
    }

    @Test
    fun invalidDate_returnsNullAndFalse() {
        assertNull(DateUtils.parseDate("2026-13-40"))
        assertFalse(DateUtils.isValidDateString("not-a-date"))
        assertFalse(DateUtils.isValidDateString(""))
    }

    @Test
    fun blankTime_isConsideredValidOptional() {
        assertTrue(DateUtils.isValidTimeString(""))
        assertTrue(DateUtils.isValidTimeString(null))
    }

    @Test
    fun invalidTime_isRejected() {
        assertFalse(DateUtils.isValidTimeString("99:99"))
    }

    @Test
    fun shortDisplay_fallsBackToRawOnInvalid() {
        assertEquals("garbage", DateUtils.shortDisplay("garbage"))
    }

    @Test
    fun daysBetween_isCorrect() {
        val today = java.time.LocalDate.of(2026, 7, 10)
        assertEquals(9L, DateUtils.daysBetween("2026-07-01", today))
    }
}
