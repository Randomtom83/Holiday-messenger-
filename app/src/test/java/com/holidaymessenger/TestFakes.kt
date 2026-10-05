package com.holidaymessenger

import com.holidaymessenger.data.db.dao.ContactDao
import com.holidaymessenger.data.db.dao.HolidayDao
import com.holidaymessenger.data.db.dao.MessageLogDao
import com.holidaymessenger.data.db.dao.MessageTemplateDao
import com.holidaymessenger.data.db.dao.ScheduledMessageDao
import com.holidaymessenger.data.db.entity.Category
import com.holidaymessenger.data.db.entity.Contact
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.data.db.entity.HolidayContactCrossRef
import com.holidaymessenger.data.db.entity.MessageLog
import com.holidaymessenger.data.db.entity.MessageTemplate
import com.holidaymessenger.data.db.entity.MessageType
import com.holidaymessenger.data.db.entity.ScheduledMessage
import com.holidaymessenger.data.review.HolidayDateSource
import com.holidaymessenger.data.review.ReviewSkipStore
import com.holidaymessenger.data.review.SendEnqueuer
import com.holidaymessenger.ui.recurring.RecurringMessageItemInternal
import com.holidaymessenger.util.HolidayPlanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/** In-memory stand-ins for the Room DAOs, shared by the JVM unit tests. */

class InMemoryHolidayDao : HolidayDao {
    val holidays = linkedMapOf<Long, Holiday>()
    val crossRefs = mutableListOf<HolidayContactCrossRef>()

    override fun getAllHolidays(): Flow<List<Holiday>> = flowOf(holidays.values.toList())
    override suspend fun getEnabledHolidaysList() = holidays.values.filter { it.enabled }
    override suspend fun getHolidayById(id: Long) = holidays[id]
    override suspend fun insertHoliday(holiday: Holiday): Long {
        holidays[holiday.id] = holiday
        return holiday.id
    }
    override suspend fun insertHolidays(holidays: List<Holiday>) { holidays.forEach { this.holidays[it.id] = it } }
    override suspend fun updateHoliday(holiday: Holiday) { holidays[holiday.id] = holiday }
    override suspend fun deleteHoliday(holiday: Holiday) { holidays.remove(holiday.id) }
    override suspend fun insertHolidayContactCrossRef(crossRef: HolidayContactCrossRef) {
        if (!crossRefs.contains(crossRef)) crossRefs.add(crossRef)
    }
    override suspend fun deleteHolidayContactCrossRef(crossRef: HolidayContactCrossRef) { crossRefs.remove(crossRef) }
    override suspend fun insertHolidayContactCrossRefs(crossRefs: List<HolidayContactCrossRef>) {
        crossRefs.forEach { insertHolidayContactCrossRef(it) }
    }
    override suspend fun deleteAllContactsForHoliday(holidayId: Long) { crossRefs.removeAll { it.holidayId == holidayId } }
    override fun getContactIdsForHoliday(holidayId: Long): Flow<List<Long>> =
        flowOf(crossRefs.filter { it.holidayId == holidayId }.map { it.contactId })
    override suspend fun getAllHolidaysList() = holidays.values.toList()
    override suspend fun getContactIdsList(holidayId: Long) =
        crossRefs.filter { it.holidayId == holidayId }.map { it.contactId }
    override suspend fun getHolidayCount() = holidays.size
    override suspend fun deleteAllHolidays() { holidays.clear() }
}

class InMemoryTemplateDao : MessageTemplateDao {
    val templates = linkedMapOf<Long, MessageTemplate>()
    private var nextId = 1L

    override fun getAllTemplates(): Flow<List<MessageTemplate>> = flowOf(templates.values.toList())
    override fun getTemplatesByCategory(category: Category): Flow<List<MessageTemplate>> =
        flowOf(templates.values.filter { it.category == category })
    override suspend fun getTemplateById(id: Long) = templates[id]
    override suspend fun getTemplateForHoliday(holidayId: Long) =
        templates.values.firstOrNull { it.holidayId == holidayId }
    override suspend fun getFirstTemplateByCategory(category: Category) =
        templates.values.firstOrNull { it.category == category }
    override suspend fun insertTemplate(template: MessageTemplate): Long {
        val id = if (template.id == 0L) nextId++ else template.id
        templates[id] = template.copy(id = id)
        return id
    }
    override suspend fun insertTemplates(templates: List<MessageTemplate>) { templates.forEach { insertTemplate(it) } }
    override suspend fun updateTemplate(template: MessageTemplate) { templates[template.id] = template }
    override suspend fun deleteTemplate(template: MessageTemplate) { templates.remove(template.id) }
    override suspend fun deleteAllTemplates() { templates.clear() }
}

class InMemoryContactDao : ContactDao {
    val contacts = linkedMapOf<Long, Contact>()

    override fun getAllContacts(): Flow<List<Contact>> = flowOf(contacts.values.sortedBy { it.name })
    override fun getContactsByRecentlyTexted(): Flow<List<Contact>> = flowOf(contacts.values.toList())
    override fun getContactsByOldestTexted(): Flow<List<Contact>> = flowOf(contacts.values.toList())
    override suspend fun getContactById(id: Long) = contacts[id]
    override fun getContactsWithBirthdays(): Flow<List<Contact>> =
        flowOf(contacts.values.filter { it.birthday != null || it.birthdayOverride != null })
    override suspend fun insertContact(contact: Contact) { contacts[contact.id] = contact }
    override suspend fun insertContacts(contacts: List<Contact>) { contacts.forEach { this.contacts[it.id] = it } }
    override suspend fun updateContact(contact: Contact) { contacts[contact.id] = contact }
    override suspend fun deleteContact(contact: Contact) { contacts.remove(contact.id) }
    override suspend fun getContactsByBirthday(monthDay: String): List<Contact> =
        contacts.values.filter { (it.birthdayOverride?.takeIf { o -> o.isNotEmpty() } ?: it.birthday) == monthDay }
}

open class InMemoryScheduledMessageDao : ScheduledMessageDao {
    val rows = linkedMapOf<Long, ScheduledMessage>()
    private var nextId = 1L

    /** When true, reads and inserts suspend once first, as Room does, so coroutines can interleave. */
    var yieldOnAccess = false

    override fun getAllScheduledMessages(): Flow<List<ScheduledMessage>> = flowOf(rows.values.toList())
    override suspend fun getEnabledMessages() = rows.values.filter { it.enabled }
    override suspend fun getEnabledMessagesByType(type: MessageType) =
        rows.values.filter { it.enabled && it.type == type }
    override suspend fun getScheduledMessageById(id: Long) = rows[id]
    override fun getScheduledMessagesByType(type: MessageType): Flow<List<ScheduledMessage>> =
        flowOf(rows.values.filter { it.type == type })
    override suspend fun getMessagesByTypeList(type: MessageType): List<ScheduledMessage> {
        if (yieldOnAccess) kotlinx.coroutines.yield()
        return rows.values.filter { it.type == type }
    }
    override suspend fun insertScheduledMessage(message: ScheduledMessage): Long {
        if (yieldOnAccess) kotlinx.coroutines.yield()
        val id = if (message.id == 0L) nextId++ else message.id
        rows[id] = message.copy(id = id)
        return id
    }
    override suspend fun updateScheduledMessage(message: ScheduledMessage) { rows[message.id] = message }
    override suspend fun deleteScheduledMessage(message: ScheduledMessage) { rows.remove(message.id) }
    override suspend fun updateSentStatus(id: Long, date: String, nextTime: Long?) {
        rows[id]?.let { rows[id] = it.copy(lastSentDate = date, nextScheduledTime = nextTime) }
    }
    override suspend fun updateScheduledDate(id: Long, date: String) {
        rows[id]?.let { rows[id] = it.copy(lastScheduledDate = date) }
    }
    override suspend fun clearScheduledDate(id: Long) {
        rows[id]?.let { rows[id] = it.copy(lastScheduledDate = null) }
    }
    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        rows[id]?.let { rows[id] = it.copy(enabled = enabled) }
    }
    override fun getRecurringMessageItems(type: MessageType): Flow<List<RecurringMessageItemInternal>> =
        flowOf(emptyList())
}

class InMemoryMessageLogDao : MessageLogDao {
    val logs = mutableListOf<MessageLog>()

    override fun getAllLogs(): Flow<List<MessageLog>> = flowOf(logs.toList())
    override fun getRecentLogs(limit: Int): Flow<List<MessageLog>> = flowOf(logs.take(limit))
    override fun getLogsForContact(contactId: Long): Flow<List<MessageLog>> =
        flowOf(logs.filter { it.contactId == contactId })
    override suspend fun insertLog(log: MessageLog): Long { logs.add(log); return logs.size.toLong() }
    override suspend fun deleteOldLogs(beforeTimestamp: Long) { logs.removeAll { it.sentAt < beforeTimestamp } }
}

class RecordingSendEnqueuer : SendEnqueuer {
    data class Call(val rowId: Long, val windowStart: Int, val windowEnd: Int, val date: LocalDate, val message: String)
    val calls = mutableListOf<Call>()
    override fun enqueue(
        scheduledMessageId: Long,
        windowStartMinutes: Int,
        windowEndMinutes: Int,
        date: LocalDate,
        message: String
    ) {
        calls.add(Call(scheduledMessageId, windowStartMinutes, windowEndMinutes, date, message))
    }
}

class InMemorySkipStore : ReviewSkipStore {
    val skipped = mutableSetOf<String>()
    override fun isSkipped(key: String) = skipped.contains(key)
    override fun skip(key: String) { skipped.add(key) }
    override fun unskip(key: String) { skipped.remove(key) }
}

class FixedDateSource(var holidays: List<HolidayPlanner.DatedHoliday> = emptyList()) : HolidayDateSource {
    override fun datesForYear(year: Int) = holidays.filter { it.date.year == year }
}
