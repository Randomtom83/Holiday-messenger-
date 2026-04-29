package com.holidaymessenger.data.repository

import com.holidaymessenger.data.db.dao.HolidayDao
import com.holidaymessenger.data.db.dao.MessageTemplateDao
import com.holidaymessenger.data.db.entity.*
import com.holidaymessenger.util.HolidayCalendar
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val holidayDao: HolidayDao,
    private val templateDao: MessageTemplateDao
) {
    fun getAllHolidays(): Flow<List<Holiday>> = holidayDao.getAllHolidays()

    suspend fun getEnabledHolidays(): List<Holiday> = holidayDao.getEnabledHolidaysList()

    suspend fun getHolidayById(id: Long): Holiday? = holidayDao.getHolidayById(id)

    suspend fun updateHoliday(holiday: Holiday) = holidayDao.updateHoliday(holiday)

    /**
     * Inserts a user-created holiday. Pass [monthDay] in "MM-dd" form for a
     * fixed-date holiday, or null for a "squad-style" entry (no scheduled date,
     * manual sends only).
     */
    suspend fun addHoliday(name: String, monthDay: String?): Long =
        holidayDao.insertHoliday(
            Holiday(name = name.trim(), monthDay = monthDay, isVariable = false, enabled = true)
        )

    suspend fun deleteHoliday(holiday: Holiday) {
        holidayDao.deleteAllContactsForHoliday(holiday.id)
        holidayDao.deleteHoliday(holiday)
    }

    fun getContactIdsForHoliday(holidayId: Long): Flow<List<Long>> =
        holidayDao.getContactIdsForHoliday(holidayId)

    suspend fun getContactIdsForHolidayList(holidayId: Long): List<Long> =
        holidayDao.getContactIdsList(holidayId)

    suspend fun addContactToHoliday(holidayId: Long, contactId: Long) {
        holidayDao.insertHolidayContactCrossRef(
            HolidayContactCrossRef(holidayId, contactId)
        )
    }

    suspend fun removeContactFromHoliday(holidayId: Long, contactId: Long) {
        holidayDao.deleteHolidayContactCrossRef(
            HolidayContactCrossRef(holidayId, contactId)
        )
    }

    suspend fun clearAllContactsForHoliday(holidayId: Long) {
        holidayDao.deleteAllContactsForHoliday(holidayId)
    }

    /**
     * Synchronizes contacts from a source holiday to a list of target holidays.
     * Performs an OVERWRITE sync to ensure targets match the source exactly.
     */
    suspend fun syncContactsToAllHolidays(sourceHolidayId: Long, includeSquad: Boolean = false) {
        val contactIds = holidayDao.getContactIdsList(sourceHolidayId)
        val allHolidays = holidayDao.getAllHolidaysList()
        
        // Target holiday IDs (all holidays except source)
        val targetHolidayIds = allHolidays.map { it.id }.filter { it != sourceHolidayId }.toMutableList()
        
        // If source is a holiday and we want to sync to squad too
        if (includeSquad && sourceHolidayId != 0L) {
            targetHolidayIds.add(0L)
        }
        
        overwriteContactsInHolidays(targetHolidayIds, contactIds)
    }

    suspend fun overwriteContactsInHolidays(holidayIds: List<Long>, contactIds: List<Long>) {
        for (hid in holidayIds) {
            holidayDao.deleteAllContactsForHoliday(hid)
            val crossRefs = contactIds.map { cid -> HolidayContactCrossRef(hid, cid) }
            if (crossRefs.isNotEmpty()) {
                holidayDao.insertHolidayContactCrossRefs(crossRefs)
            }
        }
    }

    suspend fun addContactsToHolidays(holidayIds: List<Long>, contactIds: List<Long>) {
        val crossRefs = holidayIds.flatMap { hid ->
            contactIds.map { cid -> HolidayContactCrossRef(hid, cid) }
        }
        holidayDao.insertHolidayContactCrossRefs(crossRefs)
    }

    suspend fun getEnabledHolidaysList(): List<Holiday> = holidayDao.getEnabledHolidaysList()

    suspend fun getTemplateForHoliday(holidayId: Long): MessageTemplate? =
        templateDao.getTemplateForHoliday(holidayId)

    suspend fun saveTemplateForHoliday(holidayId: Long, text: String) {
        val existing = templateDao.getTemplateForHoliday(holidayId)
        if (existing != null) {
            templateDao.updateTemplate(existing.copy(text = text))
        } else {
            templateDao.insertTemplate(
                MessageTemplate(
                    category = Category.HOLIDAY,
                    text = text,
                    holidayId = holidayId
                )
            )
        }
    }

    /**
     * Seeds the database with default holidays and templates if empty.
     */
    suspend fun seedDefaultHolidays() {
        val count = holidayDao.getHolidayCount()
        // If we have fewer than 36, we're missing the expanded holiday set
        if (count >= 36) return

        // Clear and re-seed to keep IDs consistent with templates
        holidayDao.deleteAllHolidays()
        templateDao.deleteAllTemplates()

        val holidays = listOf(
            Holiday(id = 1, name = "New Year's Day", monthDay = "01-01"),
            Holiday(id = 2, name = "Martin Luther King Jr. Day", isVariable = true),
            Holiday(id = 3, name = "Lunar New Year", isVariable = true),
            Holiday(id = 4, name = "Presidents' Day", isVariable = true),
            Holiday(id = 5, name = "Valentine's Day", monthDay = "02-14"),
            Holiday(id = 6, name = "International Women's Day", monthDay = "03-08"),
            Holiday(id = 7, name = "St. Patrick's Day", monthDay = "03-17"),
            Holiday(id = 8, name = "Easter", isVariable = true),
            Holiday(id = 9, name = "Passover", isVariable = true),
            Holiday(id = 10, name = "Earth Day", monthDay = "04-22"),
            Holiday(id = 32, name = "Star Wars Day", monthDay = "05-04"),
            Holiday(id = 11, name = "Cinco de Mayo", monthDay = "05-05"),
            Holiday(id = 31, name = "Teacher Appreciation Day", isVariable = true),
            Holiday(id = 33, name = "National Nurses Day", monthDay = "05-06"),
            Holiday(id = 12, name = "Mother's Day", isVariable = true),
            Holiday(id = 34, name = "Armed Forces Day", isVariable = true),
            Holiday(id = 13, name = "Memorial Day", isVariable = true),
            Holiday(id = 14, name = "Father's Day", isVariable = true),
            Holiday(id = 35, name = "Pride Month", monthDay = "06-01"),
            Holiday(id = 36, name = "World Environment Day", monthDay = "06-05"),
            Holiday(id = 15, name = "Flag Day", monthDay = "06-14"),
            Holiday(id = 16, name = "Juneteenth", monthDay = "06-19"),
            Holiday(id = 17, name = "Eid al-Fitr", isVariable = true),
            Holiday(id = 18, name = "Independence Day", monthDay = "07-04"),
            Holiday(id = 19, name = "Eid al-Adha", isVariable = true),
            Holiday(id = 20, name = "Labor Day", isVariable = true),
            Holiday(id = 21, name = "Rosh Hashanah", isVariable = true),
            Holiday(id = 22, name = "Indigenous Peoples' Day", isVariable = true),
            Holiday(id = 23, name = "Halloween", monthDay = "10-31"),
            Holiday(id = 24, name = "Election Day", isVariable = true),
            Holiday(id = 25, name = "Veterans Day", monthDay = "11-11"),
            Holiday(id = 26, name = "Diwali", isVariable = true),
            Holiday(id = 27, name = "Thanksgiving", isVariable = true),
            Holiday(id = 28, name = "Hanukkah", isVariable = true),
            Holiday(id = 29, name = "Christmas", monthDay = "12-25"),
            Holiday(id = 30, name = "Kwanzaa", monthDay = "12-26")
        )
        holidayDao.insertHolidays(holidays)

        val defaultTemplates = listOf(
            MessageTemplate(category = Category.HOLIDAY, text = "Happy New Year, {name}! Wishing you an amazing year ahead!", holidayId = 1),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy MLK Day, {name}! His dream lives on in every act of kindness and justice.", holidayId = 2),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Lunar New Year, {name}! Wishing you prosperity and good fortune! 🧧", holidayId = 3),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Presidents' Day, {name}! Hope you're enjoying a well-earned day off.", holidayId = 4),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Valentine's Day, {name}! Sending you love today!", holidayId = 5),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy International Women's Day, {name}! Celebrating the strength and brilliance of women everywhere.", holidayId = 6),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy St. Patrick's Day, {name}! May your day be filled with good luck! 🍀", holidayId = 7),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Easter, {name}! Hope you have a wonderful day!", holidayId = 8),
            MessageTemplate(category = Category.HOLIDAY, text = "Chag Sameach, {name}! Wishing you a peaceful and happy Passover!", holidayId = 9),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Earth Day, {name}! Here's to protecting the beautiful planet we all share. 🌍", holidayId = 10),
            MessageTemplate(category = Category.HOLIDAY, text = "May the 4th be with you, {name}! Hope your day is out-of-this-galaxy good. ⭐", holidayId = 32),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Cinco de Mayo, {name}! Wishing you good food, great company, and plenty of celebration!", holidayId = 11),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Teacher Appreciation Day, {name}! Thank you for the difference you make every single day. 🍎", holidayId = 31),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy National Nurses Day, {name}! The care and compassion you bring to your work changes lives every single day. Thank you. 💙", holidayId = 33),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Mother's Day, {name}! Thank you for everything you do!", holidayId = 12),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Armed Forces Day, {name}! Thank you for your courage and commitment to keeping us all safe. 🇺🇸", holidayId = 34),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Memorial Day, {name}. Today we honor those who gave everything for our freedom.", holidayId = 13),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Father's Day, {name}! Hope you have an awesome day!", holidayId = 14),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Pride Month, {name}! Here's to celebrating love, identity, and the freedom to be exactly who you are. 🌈", holidayId = 35),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy World Environment Day, {name}! Every small choice we make for the planet adds up to something big. Thank you for caring. 🌿", holidayId = 36),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Flag Day, {name}! Proud to share this country with you. 🇺🇸", holidayId = 15),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Juneteenth, {name}! Celebrating freedom and community today!", holidayId = 16),
            MessageTemplate(category = Category.HOLIDAY, text = "Eid Mubarak, {name}! Wishing you and your family a blessed Eid al-Fitr! ✨", holidayId = 17),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy 4th of July, {name}! Have a great Independence Day!", holidayId = 18),
            MessageTemplate(category = Category.HOLIDAY, text = "Eid Mubarak, {name}! Wishing you a joyous Eid al-Adha! 🌙", holidayId = 19),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Labor Day, {name}! Hope you're getting a well-deserved break today.", holidayId = 20),
            MessageTemplate(category = Category.HOLIDAY, text = "Shana Tova, {name}! Wishing you a sweet and happy New Year! 🍎🍯", holidayId = 21),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Indigenous Peoples' Day, {name}! Celebrating the rich cultures and resilience of Native communities.", holidayId = 22),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Halloween, {name}! Have a spooky night!", holidayId = 23),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Election Day, {name}! Every vote counts! 🗳️🇺🇸", holidayId = 24),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Veterans Day, {name}. Thank you for your service and sacrifice.", holidayId = 25),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Diwali, {name}! Wishing you a festival of lights filled with joy! 🪔", holidayId = 26),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Thanksgiving, {name}! Grateful to have you in my life!", holidayId = 27),
            MessageTemplate(category = Category.HOLIDAY, text = "Hanukkah Sameach, {name}! May your home be filled with light! 🕎", holidayId = 28),
            MessageTemplate(category = Category.HOLIDAY, text = "Merry Christmas, {name}! Wishing you joy and happiness this holiday season!", holidayId = 29),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Kwanzaa, {name}! Wishing you a beautiful celebration of unity, creativity, and purpose. 🕯️", holidayId = 30),
            MessageTemplate(category = Category.BIRTHDAY, text = "Happy Birthday, {name}! Hope you have an amazing day! 🎂")
        )
        templateDao.insertTemplates(defaultTemplates)
    }
}
