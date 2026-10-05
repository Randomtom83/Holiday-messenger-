package com.holidaymessenger.data.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: Long,
    val name: String,
    val phoneNumber: String,
    val birthday: String? = null,
    val birthdayOverride: String? = null,
    val preferredChannel: Channel = Channel.SMS,
    val lastSmsSentTimestamp: Long? = null,
    val birthdayTemplateId: Long? = null
) {
    @get:Ignore
    val effectiveBirthday: String?
        get() = birthdayOverride ?: birthday
}
