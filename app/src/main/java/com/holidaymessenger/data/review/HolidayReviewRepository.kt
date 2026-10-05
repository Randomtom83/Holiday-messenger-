package com.holidaymessenger.data.review

import com.holidaymessenger.data.db.entity.Channel
import com.holidaymessenger.data.db.entity.Contact
import com.holidaymessenger.data.db.entity.Frequency
import com.holidaymessenger.data.db.entity.MessageType
import com.holidaymessenger.data.db.entity.ScheduledMessage
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.HolidayRepository
import com.holidaymessenger.data.repository.MessageRepository
import com.holidaymessenger.util.HolidayPlanner
import com.holidaymessenger.util.HolidayPlanner.ReviewState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** One person's holiday message, ready for the owner to approve or skip. */
data class ReviewItem(
    val key: String,
    val date: LocalDate,
    val holidayId: Long,
    val holidayName: String,
    val contactId: Long,
    val contactName: String,
    val channel: Channel,
    val isGroup: Boolean,
    val text: String,
    val state: ReviewState
)

/**
 * Review-first holiday sending. The queue comes straight from the holiday contact lists, so removing a
 * person from a holiday removes them from the queue. Nothing is sent until [approve] is called.
 *
 * Approving creates (or reuses) a disabled HOLIDAY [ScheduledMessage] row as plumbing for the sender
 * worker. The row is always disabled so the daily scheduler can never send it on its own.
 */
@Singleton
class HolidayReviewRepository @Inject constructor(
    private val holidayRepository: HolidayRepository,
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val sendEnqueuer: SendEnqueuer,
    private val skipStore: ReviewSkipStore,
    private val dateSource: HolidayDateSource
) {
    // One approval at a time: two overlapping approvals must not create two rows or queue two sends.
    private val approveLock = Mutex()

    /** Items for holidays happening from [today] through [today] plus [LOOKAHEAD_DAYS], in date order. */
    suspend fun queue(today: LocalDate = LocalDate.now()): List<ReviewItem> {
        val holidays = holidayRepository.getEnabledHolidays()
        val defs = holidays.map { HolidayPlanner.HolidayDef(it.id, it.name, it.monthDay, it.enabled) }
        val lastYear = today.plusDays(LOOKAHEAD_DAYS).year
        val calendar = (today.year..lastYear).flatMap { dateSource.datesForYear(it) }
        val occurrences = HolidayPlanner.upcomingOccurrences(today, LOOKAHEAD_DAYS, defs, calendar)
        if (occurrences.isEmpty()) return emptyList()

        val marks = messageRepository.getMessagesByType(MessageType.HOLIDAY)
            .associateBy { it.holidayId to it.contactId }

        val items = mutableListOf<ReviewItem>()
        for (occurrence in occurrences) {
            val template = holidayRepository.getTemplateForHoliday(occurrence.holidayId) ?: continue
            val contactIds = holidayRepository.getContactIdsForHolidayList(occurrence.holidayId).distinct()
            for (contactId in contactIds) {
                val contact = contactRepository.getContactById(contactId) ?: continue
                val key = HolidayPlanner.itemKey(occurrence.date, occurrence.holidayId, contactId)
                val row = marks[occurrence.holidayId to contactId]
                val mark = row?.let {
                    HolidayPlanner.RowMark(
                        holidayId = occurrence.holidayId,
                        contactId = contactId,
                        lastScheduledDate = it.lastScheduledDate,
                        lastSentDate = it.lastSentDate
                    )
                }
                items.add(
                    ReviewItem(
                        key = key,
                        date = occurrence.date,
                        holidayId = occurrence.holidayId,
                        holidayName = occurrence.holidayName,
                        contactId = contactId,
                        contactName = contact.name,
                        channel = contact.preferredChannel,
                        isGroup = contact.phoneNumber.contains(","),
                        text = messageRepository.resolveTemplate(template.text, contact.name),
                        state = HolidayPlanner.stateFor(occurrence.date, mark, skipStore.isSkipped(key))
                    )
                )
            }
        }
        return items
    }

    suspend fun pendingCount(today: LocalDate = LocalDate.now()): Int =
        queue(today).count { it.state == ReviewState.PENDING }

    /**
     * Schedules the message to send inside its window on the holiday's date, with exactly the text on screen.
     *
     * The item on screen can be stale (a double tap, an edited template, a person removed, a day that
     * passed), so the queue is rebuilt first and this only acts if the item is still pending or skipped and
     * its text is unchanged. Returns false when it did nothing.
     */
    suspend fun approve(item: ReviewItem, today: LocalDate = LocalDate.now()): Boolean =
        approveLock.withLock { approveLocked(item, today, allowSkipped = true) }

    private suspend fun approveLocked(item: ReviewItem, today: LocalDate, allowSkipped: Boolean): Boolean {
        val current = queue(today).firstOrNull { it.key == item.key } ?: return false
        if (current.state != ReviewState.PENDING && !(allowSkipped && current.state == ReviewState.SKIPPED)) return false
        if (current.text != item.text) return false

        val contact = contactRepository.getContactById(current.contactId) ?: return false
        val template = holidayRepository.getTemplateForHoliday(current.holidayId) ?: return false

        val row = ensureRow(current.holidayId, contact, template.id)
        val iso = current.date.toString()
        if (row.lastScheduledDate == iso || row.lastSentDate == iso) return false

        sendEnqueuer.enqueue(row.id, WINDOW_START_MINUTES, WINDOW_END_MINUTES, current.date, current.text)
        messageRepository.updateScheduledDate(row.id, iso)
        skipStore.unskip(current.key)
        return true
    }

    /**
     * Approves every item that is still pending. Skipped items stay skipped; approve them one by one.
     * Returns how many were scheduled.
     */
    suspend fun approveAll(items: List<ReviewItem>, today: LocalDate = LocalDate.now()): Int {
        var approved = 0
        for (item in items) {
            // Checked against live state for every item, so a skip made while this runs still holds.
            if (approveLock.withLock { approveLocked(item, today, allowSkipped = false) }) approved++
        }
        return approved
    }

    /**
     * Whether an approved holiday send should still go out: the holiday is still on and the person is still
     * on its list. Called by the sender worker just before sending, so removing someone cancels their send.
     */
    suspend fun canSend(row: ScheduledMessage): Boolean {
        val holidayId = row.holidayId ?: return false
        val holiday = holidayRepository.getHolidayById(holidayId) ?: return false
        if (!holiday.enabled) return false
        return holidayRepository.getContactIdsForHolidayList(holidayId).contains(row.contactId)
    }

    fun skip(item: ReviewItem) {
        if (item.state == ReviewState.PENDING) skipStore.skip(item.key)
    }

    fun skipAll(items: List<ReviewItem>) {
        items.forEach { skip(it) }
    }

    private suspend fun ensureRow(holidayId: Long, contact: Contact, templateId: Long): ScheduledMessage {
        val existing = messageRepository.getMessagesByType(MessageType.HOLIDAY)
            .lastOrNull { it.holidayId == holidayId && it.contactId == contact.id }
        if (existing != null) {
            if (!existing.enabled && existing.templateId == templateId && existing.channel == contact.preferredChannel) {
                return existing
            }
            val updated = existing.copy(
                enabled = false,
                templateId = templateId,
                channel = contact.preferredChannel
            )
            messageRepository.updateScheduledMessage(updated)
            return updated
        }
        val created = ScheduledMessage(
            contactId = contact.id,
            templateId = templateId,
            type = MessageType.HOLIDAY,
            channel = contact.preferredChannel,
            frequency = Frequency.YEARLY,
            windowStartMinutes = WINDOW_START_MINUTES,
            windowEndMinutes = WINDOW_END_MINUTES,
            enabled = false,
            holidayId = holidayId
        )
        return created.copy(id = messageRepository.insertScheduledMessage(created))
    }

    companion object {
        /** Today and tomorrow. */
        const val LOOKAHEAD_DAYS = 1L
        /** 9:00 AM, same window birthdays use. */
        const val WINDOW_START_MINUTES = 540
        /** 11:00 AM. */
        const val WINDOW_END_MINUTES = 660
    }
}
