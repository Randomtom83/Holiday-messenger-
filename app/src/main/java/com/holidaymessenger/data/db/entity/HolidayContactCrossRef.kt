package com.holidaymessenger.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "holiday_contact_cross_ref",
    primaryKeys = ["holidayId", "contactId"]
)
data class HolidayContactCrossRef(
    val holidayId: Long,
    val contactId: Long
)
