package com.holidaymessenger.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

object TimeRandomizer {

    /**
     * Returns a random epoch millis timestamp for today (or a given date)
     * within the window [startMinutes, endMinutes] from midnight.
     *
     * @param windowStartMinutes minutes from midnight (e.g. 660 = 11:00 AM)
     * @param windowEndMinutes minutes from midnight (e.g. 840 = 2:00 PM)
     * @param date the date to schedule for (defaults to today)
     * @param random Random instance for testability
     */
    fun randomTimeInWindow(
        windowStartMinutes: Int,
        windowEndMinutes: Int,
        date: LocalDate = LocalDate.now(),
        random: Random = Random
    ): Long {
        require(windowEndMinutes > windowStartMinutes) {
            "Window end ($windowEndMinutes) must be after start ($windowStartMinutes)"
        }
        val randomMinute = random.nextInt(windowStartMinutes, windowEndMinutes)
        val hours = randomMinute / 60
        val minutes = randomMinute % 60
        val dateTime = LocalDateTime.of(date, LocalTime.of(hours, minutes))
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Returns the delay in milliseconds from now to the target time.
     * Returns null if the target time is in the past.
     */
    fun delayFromNow(targetTimeMillis: Long): Long? {
        val delay = targetTimeMillis - System.currentTimeMillis()
        return if (delay > 0) delay else null
    }
}
