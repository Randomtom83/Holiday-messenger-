package com.holidaymessenger.data.db.entity

import androidx.room.Entity

import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "holiday_contact_cross_ref",
    primaryKeys = ["holidayId", "contactId"],
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId")]
)
data class HolidayContactCrossRef(
    val holidayId: Long,
    val contactId: Long
)
