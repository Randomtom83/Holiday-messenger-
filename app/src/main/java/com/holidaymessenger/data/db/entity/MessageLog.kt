package com.holidaymessenger.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_log")
data class MessageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val contactName: String,
    val message: String,
    val channel: Channel,
    val sentAt: Long,
    val status: MessageStatus
)
