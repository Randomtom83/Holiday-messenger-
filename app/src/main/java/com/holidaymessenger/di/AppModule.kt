package com.holidaymessenger.di

import android.content.Context
import androidx.room.Room
import com.holidaymessenger.data.db.AppDatabase
import com.holidaymessenger.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "holiday_messenger.db"
        ).build()
    }

    @Provides
    fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()

    @Provides
    fun provideHolidayDao(db: AppDatabase): HolidayDao = db.holidayDao()

    @Provides
    fun provideMessageTemplateDao(db: AppDatabase): MessageTemplateDao = db.messageTemplateDao()

    @Provides
    fun provideScheduledMessageDao(db: AppDatabase): ScheduledMessageDao = db.scheduledMessageDao()

    @Provides
    fun provideMessageLogDao(db: AppDatabase): MessageLogDao = db.messageLogDao()
}
