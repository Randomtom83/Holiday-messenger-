package com.holidaymessenger.worker

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.holidaymessenger.HolidayMessengerApp
import com.holidaymessenger.R
import android.app.PendingIntent
import android.content.Intent
import com.holidaymessenger.MainActivity
import com.holidaymessenger.data.db.entity.Channel
import com.holidaymessenger.data.db.entity.MessageLog
import com.holidaymessenger.data.db.entity.MessageStatus
import com.holidaymessenger.data.db.entity.MessageType
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.MessageRepository
import com.holidaymessenger.data.review.HolidayReviewRepository
import com.holidaymessenger.messaging.SmsSender
import com.holidaymessenger.messaging.WhatsAppSender
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val smsSender: SmsSender,
    private val whatsAppSender: WhatsAppSender,
    private val holidayReviewRepository: HolidayReviewRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val scheduledMessageId = inputData.getLong(KEY_SCHEDULED_MESSAGE_ID, -1)
        if (scheduledMessageId == -1L) return Result.failure()

        val scheduledMessage = messageRepository.getScheduledMessageById(scheduledMessageId)
            ?: return Result.failure()

        // An approved holiday message that is still waiting after its day is never sent late.
        val targetDate = inputData.getString(KEY_TARGET_DATE)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (targetDate != null && LocalDate.now().isAfter(targetDate)) return Result.failure()

        // A holiday message is only ever sent from an approval, which always carries the reviewed text
        // and its date. Anything else for a HOLIDAY row is refused rather than sent unreviewed.
        val frozenMessage = inputData.getString(KEY_MESSAGE)
        if (scheduledMessage.type == MessageType.HOLIDAY && (frozenMessage == null || targetDate == null)) {
            return Result.failure()
        }

        // Removing the person or turning the holiday off after approving cancels the send.
        if (scheduledMessage.type == MessageType.HOLIDAY &&
            !holidayReviewRepository.canSend(scheduledMessage)
        ) {
            return Result.failure()
        }

        val contact = contactRepository.getContactById(scheduledMessage.contactId)
            ?: return Result.failure()

        // Reviewed messages carry the exact approved text; older rows resolve the template as before.
        val resolvedMessage = frozenMessage ?: run {
            val template = messageRepository.getTemplateById(scheduledMessage.templateId)
                ?: return Result.failure()
            messageRepository.resolveTemplate(template.text, contact.name)
        }

        val isGroup = contact.phoneNumber.contains(",")
        return when {
            // Group chats and WhatsApp cannot be sent silently. They become a notification the owner taps,
            // so the message is only handed off if that notification can actually be shown.
            isGroup || scheduledMessage.channel == Channel.WHATSAPP -> {
                val channel = if (isGroup) Channel.SMS else Channel.WHATSAPP
                val handedOff = if (isGroup) {
                    showGroupNotification(contact.name, contact.phoneNumber, resolvedMessage)
                } else {
                    showWhatsAppNotification(contact.name, contact.phoneNumber, resolvedMessage)
                }
                if (handedOff) {
                    // Marked as sent because it's now in the user's hands
                    logAndUpdateStatus(scheduledMessageId, contact.id, contact.name, resolvedMessage, channel, MessageStatus.SENT, true)
                    Result.success()
                } else {
                    logAndUpdateStatus(scheduledMessageId, contact.id, contact.name, resolvedMessage, channel, MessageStatus.FAILED, false)
                    reopenForReview(scheduledMessage)
                    Result.failure()
                }
            }
            else -> {
                val success = smsSender.send(contact.phoneNumber, resolvedMessage)
                val status = if (success) MessageStatus.SENT else MessageStatus.FAILED

                logAndUpdateStatus(scheduledMessageId, contact.id, contact.name, resolvedMessage, Channel.SMS, status, success)
                sendNotification(contact.name, status)

                when {
                    success -> Result.success()
                    runAttemptCount + 1 >= MAX_ATTEMPTS -> {
                        reopenForReview(scheduledMessage)
                        Result.failure()
                    }
                    else -> Result.retry()
                }
            }
        }
    }

    /**
     * A reviewed holiday message that could not be delivered goes back to "needs review", so the owner can
     * approve it again. Other kinds of messages are left as they were.
     */
    private suspend fun reopenForReview(scheduledMessage: com.holidaymessenger.data.db.entity.ScheduledMessage) {
        if (scheduledMessage.type == MessageType.HOLIDAY) {
            messageRepository.clearScheduledDate(scheduledMessage.id)
        }
    }

    /** False if the owner has turned off notifications for the app, or just for this channel. */
    private fun canShowTapToSend(): Boolean {
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return false
        val channel = applicationContext.getSystemService(NotificationManager::class.java)
            ?.getNotificationChannel(HolidayMessengerApp.CHANNEL_MESSAGE_SENT)
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    private suspend fun logAndUpdateStatus(
        scheduledMessageId: Long,
        contactId: Long,
        contactName: String,
        message: String,
        channel: Channel,
        status: MessageStatus,
        updateSentDate: Boolean
    ) {
        messageRepository.logMessage(
            MessageLog(
                contactId = contactId,
                contactName = contactName,
                message = message,
                channel = channel,
                sentAt = System.currentTimeMillis(),
                status = status
            )
        )

        if (updateSentDate) {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            messageRepository.updateSentStatus(scheduledMessageId, todayStr, null)
        }
    }

    private fun showWhatsAppNotification(contactName: String, phoneNumber: String, message: String): Boolean {
        if (!canShowTapToSend()) return false
        val intent = whatsAppSender.getWhatsAppIntent(phoneNumber, message) ?: return false
        val requestCode = (phoneNumber + message).hashCode()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, HolidayMessengerApp.CHANNEL_MESSAGE_SENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WhatsApp Magic Ready! ✨")
            .setContentText("Tap to send your festive message to $contactName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(requestCode, notification)
        return true
    }

    private fun showGroupNotification(groupName: String, phoneNumbers: String, message: String): Boolean {
        if (!canShowTapToSend()) return false
        val intent = smsSender.groupComposeIntent(phoneNumbers, message)
        val requestCode = (phoneNumbers + message).hashCode()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, HolidayMessengerApp.CHANNEL_MESSAGE_SENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Group message ready ✨")
            .setContentText("Tap to send your message to $groupName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(requestCode, notification)
        return true
    }

    private fun sendNotification(contactName: String, status: MessageStatus) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val channelId = if (status == MessageStatus.SENT)
            HolidayMessengerApp.CHANNEL_MESSAGE_SENT
        else
            HolidayMessengerApp.CHANNEL_MESSAGE_FAILED

        val title = if (status == MessageStatus.SENT)
            "Magic Delivered! ✨"
        else
            "Oh no! Festive Fumble 😵"

        val message = if (status == MessageStatus.SENT)
            "Holiday joy has been successfully sent to $contactName! 🎁"
        else
            "We couldn't reach $contactName. Tap to try again!"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val KEY_SCHEDULED_MESSAGE_ID = "scheduled_message_id"
        /** The exact reviewed text. Absent for birthday and recurring sends, which resolve the template. */
        const val KEY_MESSAGE = "message"
        /** ISO date the message is for. A reviewed message is never sent after this day. */
        const val KEY_TARGET_DATE = "target_date"
        /** Total SMS attempts for one message, counting the first. */
        private const val MAX_ATTEMPTS = 3
    }
}
