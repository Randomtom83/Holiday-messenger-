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
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun holidayDao(): HolidayDao
    abstract fun messageTemplateDao(): MessageTemplateDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun messageLogDao(): MessageLogDao
}
