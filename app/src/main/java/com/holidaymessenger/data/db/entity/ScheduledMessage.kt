package com.holidaymessenger.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "scheduled_messages",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MessageTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("contactId"),
        Index("templateId"),
        Index("holidayId")
    ]
)
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
    val lastScheduledDate: String? = null,
    val holidayId: Long? = null
)
