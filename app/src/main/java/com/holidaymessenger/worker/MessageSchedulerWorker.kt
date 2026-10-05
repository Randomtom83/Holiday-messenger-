package com.holidaymessenger.worker

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.holidaymessenger.HolidayMessengerApp
import com.holidaymessenger.MainActivity
import com.holidaymessenger.R
import com.holidaymessenger.data.db.entity.Frequency
import com.holidaymessenger.data.db.entity.MessageType
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.MessageRepository
import com.holidaymessenger.data.review.HolidayReviewRepository
import com.holidaymessenger.util.HolidayCalendar
import com.holidaymessenger.util.HolidayPlanner
import com.holidaymessenger.util.TimeRandomizer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Runs daily (scheduled at ~5 AM). Plans all messages for the day by:
 * 1. Checking for recurring messages due today
 * 2. Checking for birthday matches
 * 3. Asking the owner to review holiday messages (nothing sends until they approve)
 * 4. Sending a festive countdown notification
 * Recurring and birthday messages pick a random time in their window and enqueue a MessageSenderWorker.
 * Holiday messages are enqueued by [HolidayReviewRepository.approve] instead.
 */
@HiltWorker
class MessageSchedulerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val holidayReviewRepository: HolidayReviewRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayMonthDay = today.format(DateTimeFormatter.ofPattern("MM-dd"))

        // 1. Schedule recurring messages
        scheduleRecurringMessages(today, todayStr)

        // 2. Schedule birthday messages
        scheduleBirthdayMessages(todayMonthDay, today)

        // 3. Ask for review of holiday messages (nothing is sent until approved)
        notifyHolidayReview(today)

        // 4. Festive Countdown Notification
        sendCountdownNotification(today)

        return Result.success()
    }

    private fun sendCountdownNotification(today: LocalDate) {
        val nextHoliday = HolidayCalendar.getNextHoliday(today)
        val daysTo = ChronoUnit.DAYS.between(today, nextHoliday.date)

        val title = if (daysTo == 0L) "IT'S PARTY TIME! 🥳" else "The Magic is Brewing... ✨"
        val message = if (daysTo == 0L) 
            "Today is ${nextHoliday.name}! Check the app to review your messages."
        else 
            "Only $daysTo days until ${nextHoliday.name}! Is your Squad ready?"

        val notification = NotificationCompat.Builder(applicationContext, HolidayMessengerApp.CHANNEL_SCHEDULER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(999, notification)
    }

    private suspend fun scheduleRecurringMessages(today: LocalDate, todayStr: String) {
        val recurring = messageRepository.getEnabledMessagesByType(MessageType.RECURRING)

        for (msg in recurring) {
            if (msg.lastSentDate == todayStr || msg.lastScheduledDate == todayStr) continue

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
                enqueueMessageSend(msg.id, msg.windowStartMinutes, msg.windowEndMinutes, today, todayStr)
            }
        }
    }

    private suspend fun scheduleBirthdayMessages(todayMonthDay: String, today: LocalDate) {
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val birthdayContacts = contactRepository.getContactsByBirthday(todayMonthDay)
        val birthdayMessages = messageRepository.getEnabledMessagesByType(MessageType.BIRTHDAY)

        for (contact in birthdayContacts) {
            val msg = birthdayMessages.find { it.contactId == contact.id }
            if (msg != null) {
                if (msg.lastSentDate != todayStr && msg.lastScheduledDate != todayStr) {
                    enqueueMessageSend(msg.id, msg.windowStartMinutes, msg.windowEndMinutes, today, todayStr)
                }
            }
        }
    }

    private suspend fun notifyHolidayReview(today: LocalDate) {
        val pending = holidayReviewRepository.queue(today)
            .filter { it.state == HolidayPlanner.ReviewState.PENDING }
        if (pending.isEmpty()) return

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val holidayNames = pending.map { it.holidayName }.distinct().joinToString(", ")
        val count = pending.size
        val title = if (count == 1) "1 message to review" else "$count messages to review"

        val openApp = PendingIntent.getActivity(
            applicationContext,
            REVIEW_NOTIFICATION_ID,
            Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, HolidayMessengerApp.CHANNEL_REVIEW)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("$holidayNames: nothing sends until you approve it.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(REVIEW_NOTIFICATION_ID, notification)
    }

    private suspend fun enqueueMessageSend(
        scheduledMessageId: Long,
        windowStart: Int,
        windowEnd: Int,
        date: LocalDate,
        todayStr: String
    ) {
        val targetTime = TimeRandomizer.randomTimeInWindow(windowStart, windowEnd, date)
        val delay = TimeRandomizer.delayFromNow(targetTime)

        val workRequest = OneTimeWorkRequestBuilder<MessageSenderWorker>()
            .setInputData(
                workDataOf(
                    MessageSenderWorker.KEY_SCHEDULED_MESSAGE_ID to scheduledMessageId
                )
            )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("message_send_${scheduledMessageId}")
            .build()

        // Use UNIQUE work to prevent duplicate enqueues if scheduler runs multiple times
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "message_send_${scheduledMessageId}",
            ExistingWorkPolicy.REPLACE, // If it's already scheduled for today, replace it (refreshes timing)
            workRequest
        )

        // Mark as scheduled immediately to avoid race conditions
        messageRepository.updateScheduledDate(scheduledMessageId, todayStr)
    }

    companion object {
        const val WORK_NAME = "message_scheduler"
        private const val REVIEW_NOTIFICATION_ID = 1001

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
