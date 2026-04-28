package com.holidaymessenger.worker

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
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
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.MessageRepository
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
    private val whatsAppSender: WhatsAppSender
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val scheduledMessageId = inputData.getLong(KEY_SCHEDULED_MESSAGE_ID, -1)
        if (scheduledMessageId == -1L) return Result.failure()

        val scheduledMessage = messageRepository.getScheduledMessageById(scheduledMessageId)
            ?: return Result.failure()

        val contact = contactRepository.getContactById(scheduledMessage.contactId)
            ?: return Result.failure()

        val template = messageRepository.getTemplateById(scheduledMessage.templateId)
            ?: return Result.failure()

        val resolvedMessage = messageRepository.resolveTemplate(template.text, contact.name)

        return if (scheduledMessage.channel == Channel.WHATSAPP) {
            showWhatsAppNotification(contact.name, contact.phoneNumber, resolvedMessage)
            // Mark as sent because it's now in the user's hands
            logAndUpdateStatus(scheduledMessageId, contact.id, contact.name, resolvedMessage, Channel.WHATSAPP, MessageStatus.SENT, true)
            Result.success()
        } else {
            val success = smsSender.send(contact.phoneNumber, resolvedMessage)
            val status = if (success) MessageStatus.SENT else MessageStatus.FAILED

            logAndUpdateStatus(scheduledMessageId, contact.id, contact.name, resolvedMessage, Channel.SMS, status, success)
            sendNotification(contact.name, status)

            if (success) Result.success() else Result.retry()
        }
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

    private fun showWhatsAppNotification(contactName: String, phoneNumber: String, message: String) {
        val intent = whatsAppSender.getWhatsAppIntent(phoneNumber, message) ?: return
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            phoneNumber.hashCode(),
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
        manager.notify(phoneNumber.hashCode(), notification)
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
    }
}
