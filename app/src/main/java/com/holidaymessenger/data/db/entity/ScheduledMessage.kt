package com.holidaymessenger.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val templateId: Long,
    val type: MessageType,
    val channel: Channel,
    val frequency: Frequency,
    val windowStartMinutes: Int,
    val windowEndMinutes: Int,
    val enabled: Boolean = true,
    val lastSentDate: String? = null,
    val nextScheduledTime: Long? = null,
    val holidayId: Long? = null
)
