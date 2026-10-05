package com.holidaymessenger.data.review

import com.holidaymessenger.util.HolidayPlanner
import java.time.LocalDate

/**
 * Starts the background send of one scheduled message at a random time inside its window on [date].
 * [message] is the exact text the owner reviewed; it is sent as is, even if the template changes later.
 */
interface SendEnqueuer {
    fun enqueue(
        scheduledMessageId: Long,
        windowStartMinutes: Int,
        windowEndMinutes: Int,
        date: LocalDate,
        message: String
    )
}

/** Remembers which review items the user skipped. */
interface ReviewSkipStore {
    fun isSkipped(key: String): Boolean
    fun skip(key: String)
    fun unskip(key: String)
}

/** Holiday dates for a calendar year. */
interface HolidayDateSource {
    fun datesForYear(year: Int): List<HolidayPlanner.DatedHoliday>
}
