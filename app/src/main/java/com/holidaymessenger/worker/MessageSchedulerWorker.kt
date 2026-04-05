package com.holidaymessenger.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.holidaymessenger.data.db.entity.Frequency
import com.holidaymessenger.data.db.entity.MessageType
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.HolidayRepository
import com.holidaymessenger.data.repository.MessageRepository
import com.holidaymessenger.util.HolidayCalendar
import com.holidaymessenger.util.TimeRandomizer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Runs daily (scheduled at ~5 AM). Plans all messages for the day by:
 * 1. Checking for recurring messages due today
 * 2. Checking for birthday matches
 * 3. Checking for holiday matches
 * For each, it picks a random time in the configured window and enqueues a MessageSenderWorker.
 */
@HiltWorker
class MessageSchedulerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val holidayRepository: HolidayRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayMonthDay = today.format(DateTimeFormatter.ofPattern("MM-dd"))

        // 1. Schedule recurring messages
        scheduleRecurringMessages(today, todayStr)

        // 2. Schedule birthday messages
        scheduleBirthdayMessages(todayMonthDay, today)

        // 3. Schedule holiday messages
        scheduleHolidayMessages(today)

        return Result.success()
    }

    private suspend fun scheduleRecurringMessages(today: LocalDate, todayStr: String) {
        val recurring = messageRepository.getEnabledMessagesByType(MessageType.RECURRING)

        for (msg in recurring) {
            if (msg.lastSentDate == todayStr) continue

            val shouldSend = when (msg.frequency) {
                Frequency.DAILY -> true
                Frequency.WEEKLY -> {
                    val lastSent = msg.lastSentDate?.let { LocalDate.parse(it) }
                    lastSent == null || lastSent.plusWeeks(1) <= today
                }
                Frequency.MONTHLY -> {
                    val lastSent = msg.lastSentDate?.let { LocalDate.parse(it) }
                    lastSent == null || lastSent.plusMonths(1) <= today
                }
                else -> false
            }

            if (shouldSend) {
                enqueueMessageSend(msg.id, msg.windowStartMinutes, msg.windowEndMinutes, today)
            }
        }
    }

    private suspend fun scheduleBirthdayMessages(todayMonthDay: String, today: LocalDate) {
        val birthdayContacts = contactRepository.getContactsByBirthday(todayMonthDay)
        val birthdayMessages = messageRepository.getEnabledMessagesByType(MessageType.BIRTHDAY)

        for (contact in birthdayContacts) {
            val msg = birthdayMessages.find { it.contactId == contact.id }
            if (msg != null) {
                val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                if (msg.lastSentDate != todayStr) {
                    enqueueMessageSend(msg.id, msg.windowStartMinutes, msg.windowEndMinutes, today)
                }
            }
        }
    }

    private suspend fun scheduleHolidayMessages(today: LocalDate) {
        val todaysHolidays = HolidayCalendar.getTodaysHolidays(today)
        if (todaysHolidays.isEmpty()) return

        val enabledHolidays = holidayRepository.getEnabledHolidays()

        for (calendarHoliday in todaysHolidays) {
            val dbHoliday = enabledHolidays.find { it.name == calendarHoliday.name } ?: continue
            val holidayMessages = messageRepository.getEnabledMessagesByType(MessageType.HOLIDAY)
                .filter { it.holidayId == dbHoliday.id }

            for (msg in holidayMessages) {
                val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                if (msg.lastSentDate != todayStr) {
                    enqueueMessageSend(msg.id, msg.windowStartMinutes, msg.windowEndMinutes, today)
                }
            }
        }
    }

    private fun enqueueMessageSend(
        scheduledMessageId: Long,
        windowStart: Int,
        windowEnd: Int,
        date: LocalDate
    ) {
        val targetTime = TimeRandomizer.randomTimeInWindow(windowStart, windowEnd, date)
        val delay = TimeRandomizer.delayFromNow(targetTime) ?: return

        val workRequest = OneTimeWorkRequestBuilder<MessageSenderWorker>()
            .setInputData(
                workDataOf(
                    MessageSenderWorker.KEY_SCHEDULED_MESSAGE_ID to scheduledMessageId
                )
            )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("message_send_${scheduledMessageId}")
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    companion object {
        const val WORK_NAME = "message_scheduler"

        fun buildPeriodicRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<MessageSchedulerWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()
        }

        /**
         * Calculates delay until next 5:00 AM.
         */
        private fun calculateInitialDelay(): Long {
            val now = java.time.LocalDateTime.now()
            var nextRun = now.toLocalDate().atTime(5, 0)
            if (now.isAfter(nextRun)) {
                nextRun = nextRun.plusDays(1)
            }
            return java.time.Duration.between(now, nextRun).toMillis()
        }
    }
}
