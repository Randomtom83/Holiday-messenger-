package com.holidaymessenger.data.db.dao

import androidx.room.*
import com.holidaymessenger.data.db.entity.MessageLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageLogDao {
    @Query("SELECT * FROM message_log ORDER BY sentAt DESC")
    fun getAllLogs(): Flow<List<MessageLog>>

    @Query("SELECT * FROM message_log ORDER BY sentAt DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<MessageLog>>

    @Query("SELECT * FROM message_log WHERE contactId = :contactId ORDER BY sentAt DESC")
    fun getLogsForContact(contactId: Long): Flow<List<MessageLog>>

    @Insert
    suspend fun insertLog(log: MessageLog): Long

    @Query("DELETE FROM message_log WHERE sentAt < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)
}
