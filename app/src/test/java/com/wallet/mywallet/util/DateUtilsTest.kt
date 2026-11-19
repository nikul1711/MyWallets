package com.wallet.mywallet.util

import com.wallet.mywallet.db.entity.RecurrenceInterval
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, minute)
        }.timeInMillis

    @Test
    fun startOfDay_dropsTheTime() {
        assertEquals(millis(2026, 11, 18), DateUtils.startOfDay(millis(2026, 11, 18, 15, 42)))
    }

    @Test
    fun startOfMonth_returnsTheFirstDayOfTheMonth() {
        val now = millis(2026, 11, 18, 15, 42)
        assertEquals(millis(2026, 11, 1), DateUtils.startOfMonth(0, now))
    }

    @Test
    fun startOfMonth_movesByWholeMonths() {
        val now = millis(2026, 11, 18)
        assertEquals(millis(2026, 10, 1), DateUtils.startOfMonth(-1, now))
        assertEquals(millis(2026, 12, 1), DateUtils.startOfMonth(1, now))
        assertEquals(millis(2027, 1, 1), DateUtils.startOfMonth(2, now))
        assertEquals(millis(2026, 6, 1), DateUtils.startOfMonth(-5, now))
    }

    @Test
    fun nextDueDate_addsOneInterval() {
        val start = millis(2026, 1, 31)
        assertEquals(millis(2026, 2, 1), DateUtils.nextDueDate(start, RecurrenceInterval.DAILY))
        assertEquals(millis(2026, 2, 7), DateUtils.nextDueDate(start, RecurrenceInterval.WEEKLY))
        assertEquals(millis(2026, 2, 28), DateUtils.nextDueDate(start, RecurrenceInterval.MONTHLY))
        assertEquals(millis(2027, 1, 31), DateUtils.nextDueDate(start, RecurrenceInterval.YEARLY))
    }

    @Test
    fun daysBetween_countsWholeDaysAndKeepsTheSign() {
        val today = millis(2026, 11, 18)
        assertEquals(0, DateUtils.daysBetween(today, millis(2026, 11, 18)))
        assertEquals(3, DateUtils.daysBetween(today, millis(2026, 11, 21)))
        assertEquals(-2, DateUtils.daysBetween(today, millis(2026, 11, 16)))
    }

    @Test
    fun monthKey_matchesTheFormatUsedByTheReportQuery() {
        assertEquals("2026-11", DateUtils.monthKey(millis(2026, 11, 18)))
        assertEquals("2027-01", DateUtils.monthKey(millis(2027, 1, 1)))
    }
}
