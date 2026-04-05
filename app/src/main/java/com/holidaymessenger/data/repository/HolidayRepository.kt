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

    suspend fun getEnabledHolidays(): List<Holiday> = holidayDao.getEnabledHolidays()

    suspend fun getHolidayById(id: Long): Holiday? = holidayDao.getHolidayById(id)

    suspend fun updateHoliday(holiday: Holiday) = holidayDao.updateHoliday(holiday)

    fun getContactIdsForHoliday(holidayId: Long): Flow<List<Long>> =
        holidayDao.getContactIdsForHoliday(holidayId)

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
        if (holidayDao.getHolidayCount() > 0) return

        val holidays = listOf(
            Holiday(name = "New Year's Day", monthDay = "01-01"),
            Holiday(name = "Valentine's Day", monthDay = "02-14"),
            Holiday(name = "Easter", isVariable = true),
            Holiday(name = "Mother's Day", isVariable = true),
            Holiday(name = "Father's Day", isVariable = true),
            Holiday(name = "Independence Day", monthDay = "07-04"),
            Holiday(name = "Halloween", monthDay = "10-31"),
            Holiday(name = "Thanksgiving", isVariable = true),
            Holiday(name = "Christmas", monthDay = "12-25")
        )
        holidayDao.insertHolidays(holidays)

        val defaultTemplates = listOf(
            MessageTemplate(category = Category.HOLIDAY, text = "Happy New Year, {name}! Wishing you an amazing year ahead!", holidayId = 1),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Valentine's Day, {name}! Sending you love today!", holidayId = 2),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Easter, {name}! Hope you have a wonderful day!", holidayId = 3),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Mother's Day, {name}! Thank you for everything you do!", holidayId = 4),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Father's Day, {name}! Hope you have an awesome day!", holidayId = 5),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy 4th of July, {name}! Have a great Independence Day!", holidayId = 6),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Halloween, {name}! Have a spooky night!", holidayId = 7),
            MessageTemplate(category = Category.HOLIDAY, text = "Happy Thanksgiving, {name}! Grateful to have you in my life!", holidayId = 8),
            MessageTemplate(category = Category.HOLIDAY, text = "Merry Christmas, {name}! Wishing you joy and happiness this holiday season!", holidayId = 9),
            MessageTemplate(category = Category.BIRTHDAY, text = "Happy Birthday, {name}! Hope you have an amazing day! 🎂")
        )
        templateDao.insertTemplates(defaultTemplates)
    }
}
