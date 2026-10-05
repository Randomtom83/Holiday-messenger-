package com.holidaymessenger

import com.holidaymessenger.data.db.entity.Category
import com.holidaymessenger.data.db.entity.Channel
import com.holidaymessenger.data.db.entity.Contact
import com.holidaymessenger.data.db.entity.Frequency
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.data.db.entity.HolidayContactCrossRef
import com.holidaymessenger.data.db.entity.MessageTemplate
import com.holidaymessenger.data.db.entity.MessageType
import com.holidaymessenger.data.db.entity.ScheduledMessage
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.HolidayRepository
import com.holidaymessenger.data.repository.MessageRepository
import com.holidaymessenger.data.review.HolidayReviewRepository
import com.holidaymessenger.util.HolidayPlanner.DatedHoliday
import com.holidaymessenger.util.HolidayPlanner.ReviewState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HolidayReviewRepositoryTest {

    private val holidayDao = InMemoryHolidayDao()
    private val templateDao = InMemoryTemplateDao()
    private val contactDao = InMemoryContactDao()
    private val scheduledDao = InMemoryScheduledMessageDao()
    private val logDao = InMemoryMessageLogDao()
    private val enqueuer = RecordingSendEnqueuer()
    private val skipStore = InMemorySkipStore()
    private val dates = FixedDateSource()

    private val repo = HolidayReviewRepository(
        HolidayRepository(holidayDao, templateDao),
        ContactRepository(contactDao),
        MessageRepository(scheduledDao, templateDao, logDao),
        enqueuer,
        skipStore,
        dates
    )

    private val christmasEve = LocalDate.of(2026, 12, 24)
    private val christmasDay = LocalDate.of(2026, 12, 25)

    private suspend fun givenChristmas(enabled: Boolean = true, withTemplate: Boolean = true) {
        holidayDao.insertHoliday(Holiday(id = 29, name = "Christmas", monthDay = "12-25", enabled = enabled))
        if (withTemplate) {
            templateDao.insertTemplate(
                MessageTemplate(category = Category.HOLIDAY, text = "Merry Christmas, {name}!", holidayId = 29)
            )
        }
        dates.holidays = listOf(DatedHoliday("Christmas", christmasDay), DatedHoliday("Christmas", LocalDate.of(2027, 12, 25)))
    }

    private suspend fun givenContact(
        id: Long,
        name: String,
        phone: String = "5551230$id",
        channel: Channel = Channel.SMS,
        assignedTo: Long? = 29
    ) {
        contactDao.insertContact(Contact(id = id, name = name, phoneNumber = phone, preferredChannel = channel))
        if (assignedTo != null) holidayDao.insertHolidayContactCrossRef(HolidayContactCrossRef(assignedTo, id))
    }

    @Test
    fun `lists each assigned person with the exact text that would be sent`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        givenContact(2, "Alex")

        val items = repo.queue(christmasEve)

        assertEquals(listOf("Sam", "Alex"), items.map { it.contactName })
        assertEquals(listOf("Merry Christmas, Sam!", "Merry Christmas, Alex!"), items.map { it.text })
        assertTrue(items.all { it.state == ReviewState.PENDING && it.date == christmasDay && it.holidayName == "Christmas" })
        assertEquals(2, repo.pendingCount(christmasEve))
    }

    @Test
    fun `nothing is queued more than a day ahead`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")

        assertTrue(repo.queue(LocalDate.of(2026, 12, 20)).isEmpty())
        assertEquals(0, repo.pendingCount(LocalDate.of(2026, 12, 20)))
    }

    @Test
    fun `people removed from the holiday leave the queue`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        givenContact(2, "Alex")
        holidayDao.deleteHolidayContactCrossRef(HolidayContactCrossRef(29, 2))

        assertEquals(listOf("Sam"), repo.queue(christmasEve).map { it.contactName })
    }

    @Test
    fun `disabled holidays and holidays without a template are not queued`() = runBlocking<Unit> {
        givenChristmas(enabled = false)
        givenContact(1, "Sam")
        assertTrue(repo.queue(christmasEve).isEmpty())

        holidayDao.updateHoliday(holidayDao.holidays.getValue(29).copy(enabled = true))
        templateDao.templates.clear()
        assertTrue(repo.queue(christmasEve).isEmpty())
    }

    @Test
    fun `a contact that no longer exists is skipped`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        holidayDao.insertHolidayContactCrossRef(HolidayContactCrossRef(29, 99))

        assertEquals(listOf("Sam"), repo.queue(christmasEve).map { it.contactName })
    }

    @Test
    fun `a number with a comma is flagged as a group chat`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "The Garage", phone = "5551110001,5551110002,5551110003")
        givenContact(2, "Sam")

        val byName = repo.queue(christmasEve).associateBy { it.contactName }
        assertTrue(byName.getValue("The Garage").isGroup)
        assertFalse(byName.getValue("Sam").isGroup)
    }

    @Test
    fun `approve schedules one send and creates a disabled row`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam", channel = Channel.WHATSAPP)
        val item = repo.queue(christmasEve).single()

        assertTrue(repo.approve(item, christmasEve))

        val row = scheduledDao.rows.values.single()
        assertEquals(MessageType.HOLIDAY, row.type)
        assertEquals(29L, row.holidayId)
        assertEquals(1L, row.contactId)
        assertEquals(Channel.WHATSAPP, row.channel)
        assertEquals(Frequency.YEARLY, row.frequency)
        assertFalse("review-created rows must never be picked up by the daily auto scheduler", row.enabled)
        assertEquals(templateDao.getTemplateForHoliday(29)!!.id, row.templateId)
        assertEquals("2026-12-25", row.lastScheduledDate)
        assertEquals(
            listOf(RecordingSendEnqueuer.Call(row.id, HolidayReviewRepository.WINDOW_START_MINUTES, HolidayReviewRepository.WINDOW_END_MINUTES, christmasDay, "Merry Christmas, Sam!")),
            enqueuer.calls
        )
        assertEquals(ReviewState.APPROVED, repo.queue(christmasEve).single().state)
        assertEquals(0, repo.pendingCount(christmasEve))
    }

    @Test
    fun `approving the same item twice schedules only once`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        val stale = repo.queue(christmasEve).single()

        assertTrue(repo.approve(stale, christmasEve))
        assertFalse("a stale copy of the item must not schedule a second send", repo.approve(stale, christmasEve))

        assertEquals(1, enqueuer.calls.size)
        assertEquals(1, scheduledDao.rows.size)
    }

    @Test
    fun `nothing reaches the auto scheduler`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        givenContact(2, "Alex")
        repo.approveAll(repo.queue(christmasEve), christmasEve)

        assertEquals(2, scheduledDao.rows.size)
        assertTrue(scheduledDao.getEnabledMessagesByType(MessageType.HOLIDAY).isEmpty())
    }

    @Test
    fun `skip leaves nothing scheduled and can be undone by approving`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        givenContact(2, "Alex")
        val items = repo.queue(christmasEve)

        repo.skip(items.first { it.contactName == "Sam" })

        val afterSkip = repo.queue(christmasEve).associateBy { it.contactName }
        assertEquals(ReviewState.SKIPPED, afterSkip.getValue("Sam").state)
        assertEquals(ReviewState.PENDING, afterSkip.getValue("Alex").state)
        assertTrue(enqueuer.calls.isEmpty())
        assertTrue(scheduledDao.rows.isEmpty())
        assertEquals(1, repo.pendingCount(christmasEve))

        assertTrue(repo.approve(afterSkip.getValue("Sam"), christmasEve))
        assertEquals(ReviewState.APPROVED, repo.queue(christmasEve).first { it.contactName == "Sam" }.state)
    }

    @Test
    fun `approve all schedules pending items and leaves skipped ones alone`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        givenContact(2, "Alex")
        givenContact(3, "Robin")
        val items = repo.queue(christmasEve)
        repo.skip(items[0])
        repo.approve(items[1], christmasEve)

        val approved = repo.approveAll(repo.queue(christmasEve), christmasEve)

        assertEquals(1, approved)
        assertEquals(2, enqueuer.calls.size)
        val byName = repo.queue(christmasEve).associateBy { it.contactName }
        assertEquals(ReviewState.SKIPPED, byName.getValue("Sam").state)
        assertEquals(ReviewState.APPROVED, byName.getValue("Alex").state)
        assertEquals(ReviewState.APPROVED, byName.getValue("Robin").state)
    }

    @Test
    fun `approve all uses the live skip list even when the screen is stale`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        val staleItems = repo.queue(christmasEve)

        repo.skip(staleItems.single())

        assertEquals("a skip made after the list was shown still holds", 0, repo.approveAll(staleItems, christmasEve))
        assertTrue(enqueuer.calls.isEmpty())
    }

    @Test
    fun `skip all marks every pending item skipped`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        givenContact(2, "Alex")

        repo.skipAll(repo.queue(christmasEve))

        assertTrue(repo.queue(christmasEve).all { it.state == ReviewState.SKIPPED })
        assertTrue(enqueuer.calls.isEmpty())
    }

    @Test
    fun `an approved item cannot be skipped`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        repo.approve(repo.queue(christmasEve).single(), christmasEve)

        repo.skip(repo.queue(christmasEve).single())

        assertEquals(ReviewState.APPROVED, repo.queue(christmasEve).single().state)
    }

    @Test
    fun `next year starts pending again and reuses the same row`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        repo.approve(repo.queue(christmasEve).single(), christmasEve)
        val rowId = scheduledDao.rows.values.single().id
        scheduledDao.updateSentStatus(rowId, "2026-12-25", null)

        val nextYear = repo.queue(LocalDate.of(2027, 12, 24)).single()
        assertEquals(ReviewState.PENDING, nextYear.state)
        assertTrue(repo.approve(nextYear, LocalDate.of(2027, 12, 24)))

        assertEquals(1, scheduledDao.rows.size)
        assertEquals("2027-12-25", scheduledDao.rows.getValue(rowId).lastScheduledDate)
        assertEquals(2, enqueuer.calls.size)
    }

    @Test
    fun `an existing enabled holiday row is switched off and refreshed on approve`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam", channel = Channel.WHATSAPP)
        val oldTemplateId = templateDao.insertTemplate(MessageTemplate(category = Category.HOLIDAY, text = "old", holidayId = 5))
        val rowId = scheduledDao.insertScheduledMessage(
            ScheduledMessage(
                contactId = 1, templateId = oldTemplateId, type = MessageType.HOLIDAY, channel = Channel.SMS,
                frequency = Frequency.YEARLY, windowStartMinutes = 0, windowEndMinutes = 10, enabled = true, holidayId = 29
            )
        )

        assertTrue(repo.approve(repo.queue(christmasEve).single(), christmasEve))

        val row = scheduledDao.rows.getValue(rowId)
        assertFalse(row.enabled)
        assertEquals(Channel.WHATSAPP, row.channel)
        assertEquals(templateDao.getTemplateForHoliday(29)!!.id, row.templateId)
        assertEquals(1, scheduledDao.rows.size)
    }

    @Test
    fun `approve fails cleanly when the contact or template is gone`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        val item = repo.queue(christmasEve).single()

        contactDao.contacts.clear()
        assertFalse(repo.approve(item, christmasEve))

        givenContact(1, "Sam")
        templateDao.templates.clear()
        assertFalse(repo.approve(item, christmasEve))

        assertTrue(enqueuer.calls.isEmpty())
        assertTrue(scheduledDao.rows.isEmpty())
    }

    @Test
    fun `a custom holiday with a fixed date is queued`() = runBlocking<Unit> {
        holidayDao.insertHoliday(Holiday(id = 37, name = "Game Night", monthDay = "12-25", enabled = true))
        templateDao.insertTemplate(MessageTemplate(category = Category.HOLIDAY, text = "Game on, {name}", holidayId = 37))
        givenContact(1, "Sam", assignedTo = 37)

        val item = repo.queue(christmasEve).single()

        assertEquals("Game Night", item.holidayName)
        assertEquals("Game on, Sam", item.text)
    }

    @Test
    fun `overlapping approvals of the same item schedule exactly one send`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        val item = repo.queue(christmasEve).single()
        scheduledDao.yieldOnAccess = true

        val results = listOf(
            async { repo.approve(item, christmasEve) },
            async { repo.approve(item, christmasEve) },
            async { repo.approveAll(listOf(item), christmasEve) > 0 }
        ).awaitAll()

        assertEquals(1, results.count { it })
        assertEquals(1, scheduledDao.rows.size)
        assertEquals(1, enqueuer.calls.size)
    }

    @Test
    fun `a stale card for a day that has passed does nothing`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        val item = repo.queue(christmasEve).single()

        assertFalse(repo.approve(item, LocalDate.of(2026, 12, 26)))

        assertTrue(enqueuer.calls.isEmpty())
        assertTrue(scheduledDao.rows.isEmpty())
    }

    @Test
    fun `a stale card for someone removed or a holiday turned off does nothing`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        givenContact(2, "Alex")
        val items = repo.queue(christmasEve).associateBy { it.contactName }

        holidayDao.deleteHolidayContactCrossRef(HolidayContactCrossRef(29, 1))
        assertFalse(repo.approve(items.getValue("Sam"), christmasEve))

        holidayDao.updateHoliday(holidayDao.holidays.getValue(29).copy(enabled = false))
        assertFalse(repo.approve(items.getValue("Alex"), christmasEve))

        assertTrue(enqueuer.calls.isEmpty())
    }

    @Test
    fun `a stale card whose text changed does not send text the owner never saw`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        val item = repo.queue(christmasEve).single()
        val template = templateDao.getTemplateForHoliday(29)!!
        templateDao.updateTemplate(template.copy(text = "Bah humbug, {name}"))

        assertFalse(repo.approve(item, christmasEve))
        assertTrue(enqueuer.calls.isEmpty())

        val fresh = repo.queue(christmasEve).single()
        assertEquals("Bah humbug, Sam", fresh.text)
        assertTrue(repo.approve(fresh, christmasEve))
        assertEquals("Bah humbug, Sam", enqueuer.calls.single().message)
    }

    @Test
    fun `the approved text is frozen into the send`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")

        repo.approve(repo.queue(christmasEve).single(), christmasEve)
        templateDao.updateTemplate(templateDao.getTemplateForHoliday(29)!!.copy(text = "changed later"))

        assertEquals("Merry Christmas, Sam!", enqueuer.calls.single().message)
    }

    @Test
    fun `canSend is true until the person is removed or the holiday is turned off`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        repo.approve(repo.queue(christmasEve).single(), christmasEve)
        val row = scheduledDao.rows.values.single()

        assertTrue(repo.canSend(row))

        holidayDao.updateHoliday(holidayDao.holidays.getValue(29).copy(enabled = false))
        assertFalse(repo.canSend(row))

        holidayDao.updateHoliday(holidayDao.holidays.getValue(29).copy(enabled = true))
        assertTrue(repo.canSend(row))

        holidayDao.deleteHolidayContactCrossRef(HolidayContactCrossRef(29, 1))
        assertFalse(repo.canSend(row))
    }

    @Test
    fun `canSend is false for a row with no holiday`() = runBlocking<Unit> {
        givenChristmas()
        givenContact(1, "Sam")
        val orphan = ScheduledMessage(
            contactId = 1, templateId = 1, type = MessageType.HOLIDAY, channel = Channel.SMS,
            frequency = Frequency.YEARLY, windowStartMinutes = 0, windowEndMinutes = 10, holidayId = null
        )
        assertFalse(repo.canSend(orphan))
    }

    @Test
    fun `one contact on two holidays keeps separate state and rows per holiday`() = runBlocking<Unit> {
        givenChristmas()
        holidayDao.insertHoliday(Holiday(id = 30, name = "Kwanzaa", monthDay = "12-26", enabled = true))
        templateDao.insertTemplate(MessageTemplate(category = Category.HOLIDAY, text = "Happy Kwanzaa, {name}", holidayId = 30))
        dates.holidays = dates.holidays + DatedHoliday("Kwanzaa", LocalDate.of(2026, 12, 26))
        givenContact(1, "Sam")
        holidayDao.insertHolidayContactCrossRef(HolidayContactCrossRef(30, 1))
        val today = christmasDay

        val items = repo.queue(today).associateBy { it.holidayName }
        assertEquals(setOf("Christmas", "Kwanzaa"), items.keys)

        assertTrue(repo.approve(items.getValue("Christmas"), today))

        val after = repo.queue(today).associateBy { it.holidayName }
        assertEquals(ReviewState.APPROVED, after.getValue("Christmas").state)
        assertEquals(ReviewState.PENDING, after.getValue("Kwanzaa").state)
        assertEquals(1, scheduledDao.rows.size)

        assertTrue(repo.approve(after.getValue("Kwanzaa"), today))
        assertEquals(2, scheduledDao.rows.size)
        assertEquals(setOf(29L, 30L), scheduledDao.rows.values.map { it.holidayId }.toSet())
        assertTrue(repo.queue(today).all { it.state == ReviewState.APPROVED })
    }
}
