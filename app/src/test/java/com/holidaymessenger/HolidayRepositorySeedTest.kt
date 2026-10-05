package com.holidaymessenger

import com.holidaymessenger.data.db.dao.HolidayDao
import com.holidaymessenger.data.db.dao.MessageTemplateDao
import com.holidaymessenger.data.db.entity.Category
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.data.db.entity.HolidayContactCrossRef
import com.holidaymessenger.data.db.entity.MessageTemplate
import com.holidaymessenger.data.repository.HolidayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HolidayRepositorySeedTest {

    private class FakeHolidayDao : HolidayDao {
        val holidays = linkedMapOf<Long, Holiday>()
        private var nextId = 1000L
        var deleteAllCalls = 0
        var insertCalls = 0

        override fun getAllHolidays(): Flow<List<Holiday>> = flowOf(holidays.values.toList())
        override suspend fun getEnabledHolidaysList() = holidays.values.filter { it.enabled }
        override suspend fun getHolidayById(id: Long) = holidays[id]
        override suspend fun insertHoliday(holiday: Holiday): Long {
            val id = if (holiday.id == 0L) nextId++ else holiday.id
            holidays[id] = holiday.copy(id = id)
            return id
        }
        override suspend fun insertHolidays(holidays: List<Holiday>) {
            insertCalls++
            holidays.forEach { this.holidays[it.id] = it }
        }
        override suspend fun updateHoliday(holiday: Holiday) { holidays[holiday.id] = holiday }
        override suspend fun deleteHoliday(holiday: Holiday) { holidays.remove(holiday.id) }
        override suspend fun insertHolidayContactCrossRef(crossRef: HolidayContactCrossRef) {}
        override suspend fun deleteHolidayContactCrossRef(crossRef: HolidayContactCrossRef) {}
        override suspend fun insertHolidayContactCrossRefs(crossRefs: List<HolidayContactCrossRef>) {}
        override suspend fun deleteAllContactsForHoliday(holidayId: Long) {}
        override fun getContactIdsForHoliday(holidayId: Long): Flow<List<Long>> = flowOf(emptyList())
        override suspend fun getAllHolidaysList() = holidays.values.toList()
        override suspend fun getContactIdsList(holidayId: Long) = emptyList<Long>()
        override suspend fun getHolidayCount() = holidays.size
        override suspend fun deleteAllHolidays() {
            deleteAllCalls++
            holidays.clear()
        }
    }

    private class FakeTemplateDao : MessageTemplateDao {
        val templates = linkedMapOf<Long, MessageTemplate>()
        private var nextId = 1L
        var deleteAllCalls = 0
        var insertCalls = 0

        override fun getAllTemplates(): Flow<List<MessageTemplate>> = flowOf(templates.values.toList())
        override fun getTemplatesByCategory(category: Category): Flow<List<MessageTemplate>> =
            flowOf(templates.values.filter { it.category == category })
        override suspend fun getTemplateById(id: Long) = templates[id]
        override suspend fun getTemplateForHoliday(holidayId: Long) =
            templates.values.firstOrNull { it.holidayId == holidayId }
        override suspend fun getFirstTemplateByCategory(category: Category) =
            templates.values.firstOrNull { it.category == category }
        override suspend fun insertTemplate(template: MessageTemplate): Long {
            val id = nextId++
            templates[id] = template.copy(id = id)
            return id
        }
        override suspend fun insertTemplates(templates: List<MessageTemplate>) {
            insertCalls++
            templates.forEach { insertTemplate(it) }
        }
        override suspend fun updateTemplate(template: MessageTemplate) { templates[template.id] = template }
        override suspend fun deleteTemplate(template: MessageTemplate) { templates.remove(template.id) }
        override suspend fun deleteAllTemplates() {
            deleteAllCalls++
            templates.clear()
        }
    }

    private val holidayDao = FakeHolidayDao()
    private val templateDao = FakeTemplateDao()
    private val repo = HolidayRepository(holidayDao, templateDao)

    private fun templatesFor(holidayId: Long) = templateDao.templates.values.count { it.holidayId == holidayId }
    private fun birthdayTemplates() = templateDao.templates.values.count { it.category == Category.BIRTHDAY }

    @Test
    fun `fresh install seeds 36 holidays with one template each plus a birthday template`() = runBlocking<Unit> {
        repo.seedDefaultHolidays()

        assertEquals((1L..36L).toSet(), holidayDao.holidays.keys)
        assertTrue((1L..36L).all { templatesFor(it) == 1 })
        assertEquals(1, birthdayTemplates())
    }

    @Test
    fun `seeding again is a no-op and deletes nothing`() = runBlocking<Unit> {
        repo.seedDefaultHolidays()
        val holidayInserts = holidayDao.insertCalls
        val templateInserts = templateDao.insertCalls

        repo.seedDefaultHolidays()
        repo.seedDefaultHolidays()

        assertEquals(holidayInserts, holidayDao.insertCalls)
        assertEquals(templateInserts, templateDao.insertCalls)
        assertEquals(0, holidayDao.deleteAllCalls)
        assertEquals(0, templateDao.deleteAllCalls)
        assertEquals(36, holidayDao.holidays.size)
        assertEquals(37, templateDao.templates.size)
    }

    @Test
    fun `seeding keeps user changes`() = runBlocking<Unit> {
        repo.seedDefaultHolidays()
        holidayDao.updateHoliday(holidayDao.holidays.getValue(3L).copy(enabled = false))
        val customId = holidayDao.insertHoliday(Holiday(name = "Game Night", monthDay = "09-09"))
        templateDao.updateTemplate(templateDao.getTemplateForHoliday(5L)!!.copy(text = "my own words {name}"))
        templateDao.updateTemplate(
            templateDao.getFirstTemplateByCategory(Category.BIRTHDAY)!!.copy(text = "custom birthday {name}")
        )

        repo.seedDefaultHolidays()

        assertFalse(holidayDao.holidays.getValue(3L).enabled)
        assertEquals("Game Night", holidayDao.holidays[customId]?.name)
        assertEquals("my own words {name}", templateDao.getTemplateForHoliday(5L)!!.text)
        assertEquals(1, birthdayTemplates())
        assertEquals("custom birthday {name}", templateDao.getFirstTemplateByCategory(Category.BIRTHDAY)!!.text)
        assertEquals(0, holidayDao.deleteAllCalls)
        assertEquals(0, templateDao.deleteAllCalls)
    }

    @Test
    fun `seeding adds only the missing defaults when the seed list has grown`() = runBlocking<Unit> {
        repo.seedDefaultHolidays()
        (30L..36L).forEach { id ->
            holidayDao.holidays.remove(id)
            templateDao.templates.values.filter { it.holidayId == id }.forEach { templateDao.templates.remove(it.id) }
        }
        holidayDao.updateHoliday(holidayDao.holidays.getValue(1L).copy(enabled = false))
        templateDao.updateTemplate(templateDao.getTemplateForHoliday(1L)!!.copy(text = "old edit {name}"))

        repo.seedDefaultHolidays()

        assertEquals(36, holidayDao.holidays.size)
        assertTrue((1L..36L).all { templatesFor(it) == 1 })
        assertEquals(1, birthdayTemplates())
        assertFalse(holidayDao.holidays.getValue(1L).enabled)
        assertEquals("old edit {name}", templateDao.getTemplateForHoliday(1L)!!.text)
        assertEquals(0, holidayDao.deleteAllCalls)
        assertEquals(0, templateDao.deleteAllCalls)
    }

    @Test
    fun `seeding restores a missing template for an existing holiday`() = runBlocking<Unit> {
        repo.seedDefaultHolidays()
        templateDao.templates.values.first { it.holidayId == 10L }.let { templateDao.templates.remove(it.id) }

        repo.seedDefaultHolidays()

        assertNotNull(templateDao.getTemplateForHoliday(10L))
        assertEquals(37, templateDao.templates.size)
    }
}
