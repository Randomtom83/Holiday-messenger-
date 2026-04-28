package com.holidaymessenger.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.holidaymessenger.data.db.converter.Converters
import com.holidaymessenger.data.db.dao.*
import com.holidaymessenger.data.db.entity.*

@Database(
    entities = [
        Contact::class,
        Holiday::class,
        MessageTemplate::class,
        ScheduledMessage::class,
        MessageLog::class,
        HolidayContactCrossRef::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_messages_contactId` ON `scheduled_messages` (`contactId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_messages_templateId` ON `scheduled_messages` (`templateId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_messages_holidayId` ON `scheduled_messages` (`holidayId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_holiday_contact_cross_ref_contactId` ON `holiday_contact_cross_ref` (`contactId`)")
                database.execSQL("ALTER TABLE `scheduled_messages` ADD COLUMN `lastScheduledDate` TEXT")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `contacts` ADD COLUMN `birthdayTemplateId` INTEGER")
            }
        }
    }

    abstract fun contactDao(): ContactDao
    abstract fun holidayDao(): HolidayDao
    abstract fun messageTemplateDao(): MessageTemplateDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun messageLogDao(): MessageLogDao
}
