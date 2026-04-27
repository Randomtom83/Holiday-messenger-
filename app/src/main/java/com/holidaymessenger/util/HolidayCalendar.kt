package com.holidaymessenger.util

import android.icu.util.Calendar
import android.icu.util.ChineseCalendar
import android.icu.util.HebrewCalendar
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.Date

object HolidayCalendar {

    data class HolidayDate(val name: String, val date: LocalDate)

    /**
     * Returns all holiday dates for the given year, including variable-date holidays.
     */
    fun getHolidaysForYear(year: Int): List<HolidayDate> {
        val holidays = mutableListOf(
            HolidayDate("New Year's Day", LocalDate.of(year, Month.JANUARY, 1)),
            HolidayDate("Martin Luther King Jr. Day", nthDayOfWeekInMonth(year, Month.JANUARY, DayOfWeek.MONDAY, 3)),
            HolidayDate("Lunar New Year", computeLunarNewYear(year)),
            HolidayDate("Presidents' Day", nthDayOfWeekInMonth(year, Month.FEBRUARY, DayOfWeek.MONDAY, 3)),
            HolidayDate("Valentine's Day", LocalDate.of(year, Month.FEBRUARY, 14)),
            HolidayDate("International Women's Day", LocalDate.of(year, Month.MARCH, 8)),
            HolidayDate("St. Patrick's Day", LocalDate.of(year, Month.MARCH, 17)),
            HolidayDate("Easter", computeEaster(year)),
            HolidayDate("Passover", computeHebrewHoliday(year, HebrewCalendar.NISAN, 15)),
            HolidayDate("Earth Day", LocalDate.of(year, Month.APRIL, 22)),
            HolidayDate("Star Wars Day", LocalDate.of(year, Month.MAY, 4)),
            HolidayDate("Cinco de Mayo", LocalDate.of(year, Month.MAY, 5)),
            HolidayDate("Teacher Appreciation Day", computeTeacherAppreciationDay(year)),
            HolidayDate("National Nurses Day", LocalDate.of(year, Month.MAY, 6)),
            HolidayDate("Mother's Day", nthDayOfWeekInMonth(year, Month.MAY, DayOfWeek.SUNDAY, 2)),
            HolidayDate("Armed Forces Day", nthDayOfWeekInMonth(year, Month.MAY, DayOfWeek.SATURDAY, 3)),
            HolidayDate("Memorial Day", lastDayOfWeekInMonth(year, Month.MAY, DayOfWeek.MONDAY)),
            HolidayDate("Pride Month", LocalDate.of(year, Month.JUNE, 1)),
            HolidayDate("World Environment Day", LocalDate.of(year, Month.JUNE, 5)),
            HolidayDate("Father's Day", nthDayOfWeekInMonth(year, Month.JUNE, DayOfWeek.SUNDAY, 3)),
            HolidayDate("Flag Day", LocalDate.of(year, Month.JUNE, 14)),
            HolidayDate("Juneteenth", LocalDate.of(year, Month.JUNE, 19)),
            HolidayDate("Eid al-Fitr", computeIslamicHoliday(year, 10, 1)), // 1st of Shawwal
            HolidayDate("Independence Day", LocalDate.of(year, Month.JULY, 4)),
            HolidayDate("Eid al-Adha", computeIslamicHoliday(year, 12, 10)), // 10th of Dhu al-Hijjah
            HolidayDate("Labor Day", nthDayOfWeekInMonth(year, Month.SEPTEMBER, DayOfWeek.MONDAY, 1)),
            HolidayDate("Rosh Hashanah", computeHebrewHoliday(year, HebrewCalendar.TISHRI, 1)),
            HolidayDate("Indigenous Peoples' Day", nthDayOfWeekInMonth(year, Month.OCTOBER, DayOfWeek.MONDAY, 2)),
            HolidayDate("Halloween", LocalDate.of(year, Month.OCTOBER, 31)),
            HolidayDate("Election Day", computeElectionDay(year)),
            HolidayDate("Veterans Day", LocalDate.of(year, Month.NOVEMBER, 11)),
            HolidayDate("Diwali", computeDiwali(year)),
            HolidayDate("Thanksgiving", nthDayOfWeekInMonth(year, Month.NOVEMBER, DayOfWeek.THURSDAY, 4)),
            HolidayDate("Hanukkah", computeHebrewHoliday(year, HebrewCalendar.KISLEV, 25)),
            HolidayDate("Christmas", LocalDate.of(year, Month.DECEMBER, 25)),
            HolidayDate("Kwanzaa", LocalDate.of(year, Month.DECEMBER, 26))
        )
        return holidays.sortedBy { it.date }
    }

    /**
     * Election Day is the Tuesday after the first Monday in November.
     */
    private fun computeElectionDay(year: Int): LocalDate {
        val firstOfNov = LocalDate.of(year, Month.NOVEMBER, 1)
        val firstMonday = firstOfNov.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
        return firstMonday.plusDays(1)
    }

    /**
     * Teacher Appreciation Day (US): Tuesday of the first full Mon-Fri week of May,
     * i.e. the day after the first Monday in May.
     */
    private fun computeTeacherAppreciationDay(year: Int): LocalDate {
        val firstMonday = LocalDate.of(year, Month.MAY, 1)
            .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
        return firstMonday.plusDays(1)
    }

    /**
     * Returns the next holiday occurring after (or on) the given date.
     */
    fun getNextHoliday(date: LocalDate = LocalDate.now()): HolidayDate {
        val holidaysThisYear = getHolidaysForYear(date.year)
        val nextThisYear = holidaysThisYear.filter { !it.date.isBefore(date) }.minByOrNull { it.date }
        
        return nextThisYear ?: getHolidaysForYear(date.year + 1).minBy { it.date }
    }

    fun getTodaysHolidays(date: LocalDate = LocalDate.now()): List<HolidayDate> {
        return getHolidaysForYear(date.year).filter { it.date == date }
    }

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
     * Corrected Islamic holiday calculation.
     */
    private fun computeIslamicHoliday(year: Int, hijrahMonth: Int, hijrahDay: Int): LocalDate {
        // Start from the current Hijrah date and find the occurrence in the given Gregorian year
        val hDate = HijrahDate.now()
        val hYear = hDate.get(ChronoField.YEAR)
        
        // Find Hijrah year that overlaps with Gregorian year
        // Hijrah year 1446 is roughly 2024
        val targetHYear = year + 1446 - 2024 
        
        val date = HijrahDate.of(targetHYear, hijrahMonth, hijrahDay)
        var gregorian = LocalDate.from(date)
        
        // Ensure the date falls in the correct year if it overlaps
        if (gregorian.year < year) {
            gregorian = LocalDate.from(HijrahDate.of(targetHYear + 1, hijrahMonth, hijrahDay))
        } else if (gregorian.year > year) {
            gregorian = LocalDate.from(HijrahDate.of(targetHYear - 1, hijrahMonth, hijrahDay))
        }
        
        return gregorian
    }

    /**
     * Compute Hebrew holidays using ICU.
     */
    private fun computeHebrewHoliday(year: Int, month: Int, day: Int): LocalDate {
        val cal = HebrewCalendar()
        cal.clear()
        // Hebrew year is roughly Gregorian year + 3760
        // We set to a year that definitely contains the Gregorian year
        cal.set(HebrewCalendar.YEAR, year + 3760) 
        cal.set(HebrewCalendar.MONTH, month)
        cal.set(HebrewCalendar.DATE, day)
        
        // Find the occurrence that falls in the Gregorian 'year'
        var millis = cal.timeInMillis
        var date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        
        if (date.year < year) {
            cal.set(HebrewCalendar.YEAR, cal.get(HebrewCalendar.YEAR) + 1)
            millis = cal.timeInMillis
            date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        } else if (date.year > year) {
            cal.set(HebrewCalendar.YEAR, cal.get(HebrewCalendar.YEAR) - 1)
            millis = cal.timeInMillis
            date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        
        return date
    }

    private fun computeLunarNewYear(year: Int): LocalDate {
        val cal = ChineseCalendar()
        cal.clear()
        // Extended year for 2024 is 4721
        cal.set(ChineseCalendar.EXTENDED_YEAR, year + 2697)
        cal.set(ChineseCalendar.MONTH, 0)
        cal.set(ChineseCalendar.DATE, 1)
        
        val millis = cal.timeInMillis
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun computeDiwali(year: Int): LocalDate {
        return when(year) {
            2024 -> LocalDate.of(2024, 10, 31)
            2025 -> LocalDate.of(2025, 10, 20)
            2026 -> LocalDate.of(2026, 11, 8)
            else -> LocalDate.of(year, 11, 1)
        }
    }

    private fun lastDayOfWeekInMonth(
        year: Int,
        month: Month,
        dayOfWeek: DayOfWeek
    ): LocalDate {
        return LocalDate.of(year, month, 1)
            .with(TemporalAdjusters.lastInMonth(dayOfWeek))
    }

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
