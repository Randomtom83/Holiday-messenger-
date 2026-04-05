package com.holidaymessenger.data.db.dao

import androidx.room.*
import com.holidaymessenger.data.db.entity.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY lastSmsSentTimestamp DESC")
    fun getContactsByRecentlyTexted(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY lastSmsSentTimestamp ASC")
    fun getContactsByOldestTexted(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): Contact?

    @Query("SELECT * FROM contacts WHERE birthday IS NOT NULL OR birthdayOverride IS NOT NULL")
    fun getContactsWithBirthdays(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contacts WHERE birthday = :monthDay OR birthdayOverride = :monthDay")
    suspend fun getContactsByBirthday(monthDay: String): List<Contact>
}
