package com.wallet.mywallet.util

import com.wallet.mywallet.db.entity.RecurrenceInterval
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Date helpers. Every date in the database is "the start of a local day" in epoch milliseconds,
 * and every range is "from (inclusive) to (exclusive)".
 */
object DateUtils {

    private fun calendarAt(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }

    private fun Calendar.clearTime(): Calendar = apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /** Midnight at the start of the day that contains [millis]. */
    fun startOfDay(millis: Long): Long = calendarAt(millis).clearTime().timeInMillis

    /** Midnight at the start of today. */
    fun today(): Long = startOfDay(System.currentTimeMillis())

    /**
     * First day of a month, as the start of that day.
     * [offset] 0 is the month of [now], -1 is the month before, +1 the month after.
     */
    fun startOfMonth(offset: Int = 0, now: Long = System.currentTimeMillis()): Long =
        calendarAt(now).clearTime().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, offset)
        }.timeInMillis

    /** The next due date after [from] for a repeating bill. */
    fun nextDueDate(from: Long, interval: RecurrenceInterval): Long =
        calendarAt(from).apply {
            when (interval) {
                RecurrenceInterval.DAILY -> add(Calendar.DAY_OF_YEAR, 1)
                RecurrenceInterval.WEEKLY -> add(Calendar.WEEK_OF_YEAR, 1)
                RecurrenceInterval.MONTHLY -> add(Calendar.MONTH, 1)
                RecurrenceInterval.YEARLY -> add(Calendar.YEAR, 1)
            }
        }.timeInMillis

    /** Whole days from [today] until [date]. Negative means the date is in the past. */
    fun daysBetween(today: Long, date: Long): Int =
        Math.round((date - today).toDouble() / TimeUnit.DAYS.toMillis(1)).toInt()

    /** For example "Nov 18, 2026", in the phone's language. */
    fun formatDate(millis: Long): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))

    /** For example "November 2026". */
    fun formatMonth(millis: Long): String =
        SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(Date(millis))

    /** Short month name for chart labels, for example "Nov". */
    fun formatMonthShort(millis: Long): String =
        SimpleDateFormat("LLL", Locale.getDefault()).format(Date(millis))

    /** The key used by the monthly report query, for example "2026-11". */
    fun monthKey(millis: Long): String =
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date(millis))
}
