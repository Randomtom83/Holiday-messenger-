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

        val success = when (scheduledMessage.channel) {
            Channel.SMS -> smsSender.send(contact.phoneNumber, resolvedMessage)
            Channel.WHATSAPP -> whatsAppSender.send(contact.phoneNumber, resolvedMessage)
        }

        val status = if (success) MessageStatus.SENT else MessageStatus.FAILED

        // Log the message
        messageRepository.logMessage(
            MessageLog(
                contactId = contact.id,
                contactName = contact.name,
                message = resolvedMessage,
                channel = scheduledMessage.channel,
                sentAt = System.currentTimeMillis(),
                status = status
            )
        )

        // Update last sent date
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        messageRepository.updateSentStatus(scheduledMessageId, todayStr, null)

        // Send notification
        sendNotification(contact.name, status)

        return if (success) Result.success() else Result.retry()
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
            "Message sent to $contactName"
        else
            "Failed to send message to $contactName"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val KEY_SCHEDULED_MESSAGE_ID = "scheduled_message_id"
    }
}
