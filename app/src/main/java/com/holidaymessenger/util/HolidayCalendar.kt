package com.holidaymessenger.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters

object HolidayCalendar {

    data class HolidayDate(val name: String, val date: LocalDate)

    /**
     * Returns all holiday dates for the given year, including variable-date holidays.
     */
    fun getHolidaysForYear(year: Int): List<HolidayDate> {
        return listOf(
            HolidayDate("New Year's Day", LocalDate.of(year, Month.JANUARY, 1)),
            HolidayDate("Valentine's Day", LocalDate.of(year, Month.FEBRUARY, 14)),
            HolidayDate("Easter", computeEaster(year)),
            HolidayDate("Mother's Day", nthDayOfWeekInMonth(year, Month.MAY, DayOfWeek.SUNDAY, 2)),
            HolidayDate("Father's Day", nthDayOfWeekInMonth(year, Month.JUNE, DayOfWeek.SUNDAY, 3)),
            HolidayDate("Independence Day", LocalDate.of(year, Month.JULY, 4)),
            HolidayDate("Halloween", LocalDate.of(year, Month.OCTOBER, 31)),
            HolidayDate("Thanksgiving", nthDayOfWeekInMonth(year, Month.NOVEMBER, DayOfWeek.THURSDAY, 4)),
            HolidayDate("Christmas", LocalDate.of(year, Month.DECEMBER, 25))
        )
    }

    /**
     * Check if today matches any holiday. Returns the matching holidays.
     */
    fun getTodaysHolidays(date: LocalDate = LocalDate.now()): List<HolidayDate> {
        return getHolidaysForYear(date.year).filter { it.date == date }
    }

    /**
     * Compute Easter Sunday using the Anonymous Gregorian algorithm (computus).
     */
    fun computeEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }

    /**
     * Returns the nth occurrence of a given day of week in a month.
     * E.g., 2nd Sunday of May, 4th Thursday of November.
     */
    private fun nthDayOfWeekInMonth(
        year: Int,
        month: Month,
        dayOfWeek: DayOfWeek,
        n: Int
    ): LocalDate {
        val first = LocalDate.of(year, month, 1)
            .with(TemporalAdjusters.firstInMonth(dayOfWeek))
        return first.plusWeeks((n - 1).toLong())
    }
}
