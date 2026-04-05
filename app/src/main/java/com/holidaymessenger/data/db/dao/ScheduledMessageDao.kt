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

    @Query("UPDATE scheduled_messages SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
