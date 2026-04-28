package com.holidaymessenger.data.repository

import com.holidaymessenger.data.db.dao.MessageLogDao
import com.holidaymessenger.data.db.dao.MessageTemplateDao
import com.holidaymessenger.data.db.dao.ScheduledMessageDao
import com.holidaymessenger.data.db.entity.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val scheduledMessageDao: ScheduledMessageDao,
    private val messageTemplateDao: MessageTemplateDao,
    private val messageLogDao: MessageLogDao
) {
    // Scheduled Messages
    fun getAllScheduledMessages(): Flow<List<ScheduledMessage>> =
        scheduledMessageDao.getAllScheduledMessages()

    fun getScheduledMessagesByType(type: MessageType): Flow<List<ScheduledMessage>> =
        scheduledMessageDao.getScheduledMessagesByType(type)

    fun getRecurringMessageItems(): Flow<List<com.holidaymessenger.ui.recurring.RecurringMessageItem>> {
        return scheduledMessageDao.getRecurringMessageItems(MessageType.RECURRING).map { items ->
            items.map { item ->
                com.holidaymessenger.ui.recurring.RecurringMessageItem(
                    scheduledMessage = ScheduledMessage(
                        id = item.id,
                        contactId = item.contactId,
                        templateId = item.templateId,
                        type = item.type,
                        channel = item.channel,
                        frequency = item.frequency,
                        windowStartMinutes = item.windowStartMinutes,
                        windowEndMinutes = item.windowEndMinutes,
                        enabled = item.enabled,
                        lastSentDate = item.lastSentDate,
                        nextScheduledTime = item.nextScheduledTime,
                        lastScheduledDate = item.lastScheduledDate,
                        holidayId = item.holidayId
                    ),
                    contactName = item.contactName,
                    templateText = item.templateText
                )
            }
        }
    }

    suspend fun getEnabledMessages(): List<ScheduledMessage> =
        scheduledMessageDao.getEnabledMessages()

    suspend fun getEnabledMessagesByType(type: MessageType): List<ScheduledMessage> =
        scheduledMessageDao.getEnabledMessagesByType(type)

    suspend fun getScheduledMessageById(id: Long): ScheduledMessage? =
        scheduledMessageDao.getScheduledMessageById(id)

    suspend fun insertScheduledMessage(message: ScheduledMessage): Long =
        scheduledMessageDao.insertScheduledMessage(message)

    suspend fun updateScheduledMessage(message: ScheduledMessage) =
        scheduledMessageDao.updateScheduledMessage(message)

    suspend fun deleteScheduledMessage(message: ScheduledMessage) =
        scheduledMessageDao.deleteScheduledMessage(message)

    suspend fun updateSentStatus(id: Long, date: String, nextTime: Long?) =
        scheduledMessageDao.updateSentStatus(id, date, nextTime)

    suspend fun updateScheduledDate(id: Long, date: String) =
        scheduledMessageDao.updateScheduledDate(id, date)

    suspend fun setEnabled(id: Long, enabled: Boolean) =
        scheduledMessageDao.setEnabled(id, enabled)

    // Templates
    fun getAllTemplates(): Flow<List<MessageTemplate>> =
        messageTemplateDao.getAllTemplates()

    suspend fun getTemplateById(id: Long): MessageTemplate? =
        messageTemplateDao.getTemplateById(id)

    suspend fun getFirstTemplateByCategory(category: Category): MessageTemplate? =
        messageTemplateDao.getFirstTemplateByCategory(category)

    suspend fun insertTemplate(template: MessageTemplate): Long =
        messageTemplateDao.insertTemplate(template)

    suspend fun updateTemplate(template: MessageTemplate) =
        messageTemplateDao.updateTemplate(template)

    // Message Log
    fun getAllLogs(): Flow<List<MessageLog>> = messageLogDao.getAllLogs()

    fun getRecentLogs(limit: Int = 50): Flow<List<MessageLog>> = messageLogDao.getRecentLogs(limit)

    suspend fun logMessage(log: MessageLog): Long = messageLogDao.insertLog(log)

    /**
     * Resolves a template by replacing {name} with the contact's name.
     */
    fun resolveTemplate(templateText: String, contactName: String): String {
        return templateText.replace("{name}", contactName)
    }
}
