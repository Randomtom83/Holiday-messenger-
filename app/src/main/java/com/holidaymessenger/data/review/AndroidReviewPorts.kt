package com.holidaymessenger.data.review

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.holidaymessenger.util.HolidayCalendar
import com.holidaymessenger.util.HolidayPlanner
import com.holidaymessenger.util.Prefs
import com.holidaymessenger.util.TimeRandomizer
import com.holidaymessenger.worker.MessageSenderWorker
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/** Sends through the same worker the birthday and recurring messages use. */
class WorkManagerSendEnqueuer(private val context: Context) : SendEnqueuer {
    override fun enqueue(
        scheduledMessageId: Long,
        windowStartMinutes: Int,
        windowEndMinutes: Int,
        date: LocalDate,
        message: String
    ) {
        val targetTime = TimeRandomizer.randomTimeInWindow(windowStartMinutes, windowEndMinutes, date)
        val delay = TimeRandomizer.delayFromNow(targetTime)

        val workRequest = OneTimeWorkRequestBuilder<MessageSenderWorker>()
            .setInputData(
                workDataOf(
                    MessageSenderWorker.KEY_SCHEDULED_MESSAGE_ID to scheduledMessageId,
                    MessageSenderWorker.KEY_MESSAGE to message,
                    MessageSenderWorker.KEY_TARGET_DATE to date.toString()
                )
            )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("message_send_$scheduledMessageId")
            .build()

        // Unique per message, so approving twice never queues two sends.
        WorkManager.getInstance(context).enqueueUniqueWork(
            "message_send_$scheduledMessageId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}

/** Skipped items are kept in SharedPreferences; keys start with the ISO date so old ones are dropped. */
class PrefsReviewSkipStore(private val context: Context) : ReviewSkipStore {
    override fun isSkipped(key: String): Boolean =
        current().contains(key)

    override fun skip(key: String) = save(current() + key)

    override fun unskip(key: String) = save(current() - key)

    private fun current(): Set<String> =
        Prefs.get(context).getStringSet(KEY_SKIPPED, emptySet())?.toSet() ?: emptySet()

    private fun save(keys: Set<String>) {
        val cutoff = LocalDate.now().minusDays(KEEP_DAYS).toString()
        val recent = keys.filter { it.take(10) >= cutoff }.toSet()
        Prefs.get(context).edit().putStringSet(KEY_SKIPPED, recent).apply()
    }

    private companion object {
        const val KEY_SKIPPED = "review_skipped_items"
        const val KEEP_DAYS = 30L
    }
}

class AndroidHolidayDateSource : HolidayDateSource {
    override fun datesForYear(year: Int): List<HolidayPlanner.DatedHoliday> =
        HolidayCalendar.getHolidaysForYear(year).map { HolidayPlanner.DatedHoliday(it.name, it.date) }
}
