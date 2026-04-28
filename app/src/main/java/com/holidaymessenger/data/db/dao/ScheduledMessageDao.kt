package com.holidaymessenger.data.db.dao

import androidx.room.*
import com.holidaymessenger.data.db.entity.Frequency
import com.holidaymessenger.data.db.entity.MessageType
import com.holidaymessenger.data.db.entity.ScheduledMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {
    @Query("SELECT * FROM scheduled_messages ORDER BY windowStartMinutes ASC")
    fun getAllScheduledMessages(): Flow<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduled_messages WHERE enabled = 1")
    suspend fun getEnabledMessages(): List<ScheduledMessage>

    @Query("SELECT * FROM scheduled_messages WHERE enabled = 1 AND type = :type")
    suspend fun getEnabledMessagesByType(type: MessageType): List<ScheduledMessage>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id")
    suspend fun getScheduledMessageById(id: Long): ScheduledMessage?

    @Query("SELECT * FROM scheduled_messages WHERE type = :type")
    fun getScheduledMessagesByType(type: MessageType): Flow<List<ScheduledMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMessage(message: ScheduledMessage): Long

    @Update
    suspend fun updateScheduledMessage(message: ScheduledMessage)

    @Delete
    suspend fun deleteScheduledMessage(message: ScheduledMessage)

    @Query("UPDATE scheduled_messages SET lastSentDate = :date, nextScheduledTime = :nextTime WHERE id = :id")
    suspend fun updateSentStatus(id: Long, date: String, nextTime: Long?)

    @Query("UPDATE scheduled_messages SET lastScheduledDate = :date WHERE id = :id")
    suspend fun updateScheduledDate(id: Long, date: String)

    @Query("UPDATE scheduled_messages SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("""
        SELECT 
            sm.id as id,
            sm.contactId as contactId,
            sm.templateId as templateId,
            sm.type as type,
            sm.channel as channel,
            sm.frequency as frequency,
            sm.windowStartMinutes as windowStartMinutes,
            sm.windowEndMinutes as windowEndMinutes,
            sm.enabled as enabled,
            sm.lastSentDate as lastSentDate,
            sm.nextScheduledTime as nextScheduledTime,
            sm.lastScheduledDate as lastScheduledDate,
            sm.holidayId as holidayId,
            c.name as contactName, 
            mt.text as templateText
        FROM scheduled_messages sm
        JOIN contacts c ON sm.contactId = c.id
        JOIN message_templates mt ON sm.templateId = mt.id
        WHERE sm.type = :type
    """)
    fun getRecurringMessageItems(type: MessageType): Flow<List<com.holidaymessenger.ui.recurring.RecurringMessageItemInternal>>
}
