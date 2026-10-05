package com.holidaymessenger.util

import java.time.LocalDate

/**
 * Pure planning rules for holiday messages. No Android or database types, so it can be unit tested on the JVM.
 */
object HolidayPlanner {

    /** A holiday as stored in the database. Id 0 is the "Squad" list and is never a real holiday. */
    data class HolidayDef(
        val id: Long,
        val name: String,
        val monthDay: String?,
        val enabled: Boolean
    )

    /** A holiday name with its date, as computed by the calendar. */
    data class DatedHoliday(val name: String, val date: LocalDate)

    /** One holiday happening on one date. */
    data class Occurrence(val holidayId: Long, val holidayName: String, val date: LocalDate)

    enum class ReviewState { PENDING, APPROVED, SENT, SKIPPED }

    /** What a stored holiday send row says about when it was last scheduled and sent. */
    data class RowMark(
        val holidayId: Long,
        val contactId: Long,
        val lastScheduledDate: String?,
        val lastSentDate: String?
    )

    /**
     * Enabled holidays that fall on [today] up to [lookaheadDays] days later (inclusive).
     *
     * Dates come from [calendar] by holiday name. A holiday the calendar does not know (one the user
     * created) falls back to its fixed MM-dd [HolidayDef.monthDay]. A holiday with neither never occurs.
     */
    fun upcomingOccurrences(
        today: LocalDate,
        lookaheadDays: Long,
        holidays: List<HolidayDef>,
        calendar: List<DatedHoliday>
    ): List<Occurrence> {
        val last = today.plusDays(lookaheadDays)
        val result = mutableListOf<Occurrence>()
        for (holiday in holidays) {
            if (!holiday.enabled || holiday.id <= 0L) continue
            val fromCalendar = calendar.filter { it.name == holiday.name }.map { it.date }
            val dates = if (fromCalendar.isNotEmpty()) {
                fromCalendar
            } else {
                customDates(holiday.monthDay, today.year, last.year)
            }
            dates
                .filter { !it.isBefore(today) && !it.isAfter(last) }
                .distinct()
                .forEach { result.add(Occurrence(holiday.id, holiday.name, it)) }
        }
        return result.sortedWith(compareBy({ it.date }, { it.holidayName }))
    }

    /**
     * Where an item stands for [date]. A sent or scheduled mark for exactly that date wins; otherwise it is
     * skipped if the user skipped it, otherwise it is waiting for review.
     */
    fun stateFor(date: LocalDate, mark: RowMark?, skipped: Boolean): ReviewState {
        val iso = date.toString()
        return when {
            mark?.lastSentDate == iso -> ReviewState.SENT
            mark?.lastScheduledDate == iso -> ReviewState.APPROVED
            skipped -> ReviewState.SKIPPED
            else -> ReviewState.PENDING
        }
    }

    /** Stable id for one person on one holiday on one date. Starts with the ISO date so old keys can be pruned. */
    fun itemKey(date: LocalDate, holidayId: Long, contactId: Long): String =
        "$date:$holidayId:$contactId"

    private fun customDates(monthDay: String?, fromYear: Int, toYear: Int): List<LocalDate> {
        if (monthDay == null) return emptyList()
        val parts = monthDay.split("-")
        if (parts.size != 2) return emptyList()
        val month = parts[0].toIntOrNull() ?: return emptyList()
        val day = parts[1].toIntOrNull() ?: return emptyList()
        return (fromYear..toYear).mapNotNull { year ->
            runCatching { LocalDate.of(year, month, day) }.getOrNull()
        }
    }
}
