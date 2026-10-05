package com.holidaymessenger

import com.holidaymessenger.util.HolidayPlanner
import com.holidaymessenger.util.HolidayPlanner.DatedHoliday
import com.holidaymessenger.util.HolidayPlanner.HolidayDef
import com.holidaymessenger.util.HolidayPlanner.ReviewState
import com.holidaymessenger.util.HolidayPlanner.RowMark
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HolidayPlannerTest {

    private val today = LocalDate.of(2026, 12, 24)
    private val christmas = HolidayDef(id = 29, name = "Christmas", monthDay = "12-25", enabled = true)
    private val calendar = listOf(
        DatedHoliday("Christmas", LocalDate.of(2026, 12, 25)),
        DatedHoliday("Kwanzaa", LocalDate.of(2026, 12, 26)),
        DatedHoliday("New Year's Day", LocalDate.of(2027, 1, 1))
    )

    @Test
    fun `finds a holiday tomorrow and today but not beyond the lookahead`() {
        val kwanzaa = HolidayDef(30, "Kwanzaa", "12-26", true)

        val tomorrowOnly = HolidayPlanner.upcomingOccurrences(today, 1, listOf(christmas, kwanzaa), calendar)
        assertEquals(listOf("Christmas"), tomorrowOnly.map { it.holidayName })

        val onTheDay = HolidayPlanner.upcomingOccurrences(LocalDate.of(2026, 12, 25), 1, listOf(christmas, kwanzaa), calendar)
        assertEquals(listOf("Christmas", "Kwanzaa"), onTheDay.map { it.holidayName })
    }

    @Test
    fun `a holiday that already passed is not returned`() {
        val result = HolidayPlanner.upcomingOccurrences(LocalDate.of(2026, 12, 26), 1, listOf(christmas), calendar)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `disabled holidays and the squad list never occur`() {
        val disabled = christmas.copy(enabled = false)
        val squad = HolidayDef(0, "Christmas", "12-25", true)
        assertTrue(HolidayPlanner.upcomingOccurrences(today, 1, listOf(disabled, squad), calendar).isEmpty())
    }

    @Test
    fun `crosses the year boundary using next year's calendar`() {
        val newYear = HolidayDef(1, "New Year's Day", "01-01", true)
        val result = HolidayPlanner.upcomingOccurrences(LocalDate.of(2026, 12, 31), 1, listOf(newYear), calendar)
        assertEquals(listOf(LocalDate.of(2027, 1, 1)), result.map { it.date })
    }

    @Test
    fun `a custom holiday falls back to its fixed month and day`() {
        val gameNight = HolidayDef(37, "Game Night", "12-25", true)
        val result = HolidayPlanner.upcomingOccurrences(today, 1, listOf(gameNight), calendar)
        assertEquals(listOf(LocalDate.of(2026, 12, 25)), result.map { it.date })
    }

    @Test
    fun `a custom holiday without a date never occurs`() {
        val manualOnly = HolidayDef(38, "Squad Chat", null, true)
        assertTrue(HolidayPlanner.upcomingOccurrences(today, 1, listOf(manualOnly), calendar).isEmpty())
    }

    @Test
    fun `a custom Feb 29 holiday only occurs in a leap year`() {
        val leapDay = HolidayDef(39, "Leap Day", "02-29", true)
        assertTrue(HolidayPlanner.upcomingOccurrences(LocalDate.of(2027, 2, 28), 1, listOf(leapDay), emptyList()).isEmpty())
        val leapYear = HolidayPlanner.upcomingOccurrences(LocalDate.of(2028, 2, 28), 1, listOf(leapDay), emptyList())
        assertEquals(listOf(LocalDate.of(2028, 2, 29)), leapYear.map { it.date })
    }

    @Test
    fun `a malformed month and day is ignored`() {
        val bad = HolidayDef(40, "Broken", "tomorrow", true)
        assertTrue(HolidayPlanner.upcomingOccurrences(today, 1, listOf(bad), emptyList()).isEmpty())
    }

    @Test
    fun `occurrences are sorted by date then name`() {
        val a = HolidayDef(1, "A Day", null, true)
        val b = HolidayDef(2, "B Day", null, true)
        val cal = listOf(
            DatedHoliday("B Day", LocalDate.of(2026, 12, 24)),
            DatedHoliday("A Day", LocalDate.of(2026, 12, 25)),
            DatedHoliday("A Day", LocalDate.of(2026, 12, 24))
        )
        val result = HolidayPlanner.upcomingOccurrences(today, 1, listOf(b, a), cal)
        assertEquals(
            listOf("A Day" to LocalDate.of(2026, 12, 24), "B Day" to LocalDate.of(2026, 12, 24), "A Day" to LocalDate.of(2026, 12, 25)),
            result.map { it.holidayName to it.date }
        )
    }

    @Test
    fun `state follows the marks for that exact date`() {
        val date = LocalDate.of(2026, 12, 25)
        assertEquals(ReviewState.PENDING, HolidayPlanner.stateFor(date, null, false))
        assertEquals(ReviewState.SKIPPED, HolidayPlanner.stateFor(date, null, true))
        val scheduled = RowMark(29, 1, "2026-12-25", null)
        assertEquals(ReviewState.APPROVED, HolidayPlanner.stateFor(date, scheduled, true))
        val sent = RowMark(29, 1, "2026-12-25", "2026-12-25")
        assertEquals(ReviewState.SENT, HolidayPlanner.stateFor(date, sent, false))
    }

    @Test
    fun `last year's marks do not count this year`() {
        val lastYear = RowMark(29, 1, "2025-12-25", "2025-12-25")
        assertEquals(ReviewState.PENDING, HolidayPlanner.stateFor(LocalDate.of(2026, 12, 25), lastYear, false))
    }

    @Test
    fun `item keys start with the ISO date`() {
        assertEquals("2026-12-25:29:7", HolidayPlanner.itemKey(LocalDate.of(2026, 12, 25), 29, 7))
    }
}
