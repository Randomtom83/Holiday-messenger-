package com.holidaymessenger

import com.holidaymessenger.util.HolidayCalendar
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class HolidayCalendarTest {

    @Test
    fun `Easter 2024 is March 31`() {
        val easter = HolidayCalendar.computeEaster(2024)
        assertEquals(LocalDate.of(2024, Month.MARCH, 31), easter)
    }

    @Test
    fun `Easter 2025 is April 20`() {
        val easter = HolidayCalendar.computeEaster(2025)
        assertEquals(LocalDate.of(2025, Month.APRIL, 20), easter)
    }

    @Test
    fun `Easter 2026 is April 5`() {
        val easter = HolidayCalendar.computeEaster(2026)
        assertEquals(LocalDate.of(2026, Month.APRIL, 5), easter)
    }

    @Test
    fun `getHolidaysForYear returns 9 holidays`() {
        val holidays = HolidayCalendar.getHolidaysForYear(2024)
        assertEquals(9, holidays.size)
    }

    @Test
    fun `Christmas is always Dec 25`() {
        val holidays = HolidayCalendar.getHolidaysForYear(2024)
        val christmas = holidays.find { it.name == "Christmas" }
        assertNotNull(christmas)
        assertEquals(LocalDate.of(2024, Month.DECEMBER, 25), christmas!!.date)
    }

    @Test
    fun `Thanksgiving 2024 is Nov 28`() {
        val holidays = HolidayCalendar.getHolidaysForYear(2024)
        val thanksgiving = holidays.find { it.name == "Thanksgiving" }
        assertNotNull(thanksgiving)
        assertEquals(LocalDate.of(2024, Month.NOVEMBER, 28), thanksgiving!!.date)
    }

    @Test
    fun `Mothers Day 2024 is May 12`() {
        val holidays = HolidayCalendar.getHolidaysForYear(2024)
        val mothersDay = holidays.find { it.name == "Mother's Day" }
        assertNotNull(mothersDay)
        assertEquals(LocalDate.of(2024, Month.MAY, 12), mothersDay!!.date)
    }

    @Test
    fun `getTodaysHolidays returns Christmas on Dec 25`() {
        val result = HolidayCalendar.getTodaysHolidays(LocalDate.of(2024, 12, 25))
        assertEquals(1, result.size)
        assertEquals("Christmas", result[0].name)
    }

    @Test
    fun `getTodaysHolidays returns empty for regular day`() {
        val result = HolidayCalendar.getTodaysHolidays(LocalDate.of(2024, 6, 15))
        assertTrue(result.isEmpty())
    }
}
