package com.holidaymessenger.data.repository

import com.holidaymessenger.data.db.dao.ContactDao
import com.holidaymessenger.data.db.entity.Contact
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()

    fun getContactsByRecentlyTexted(): Flow<List<Contact>> = contactDao.getContactsByRecentlyTexted()

    fun getContactsByOldestTexted(): Flow<List<Contact>> = contactDao.getContactsByOldestTexted()

    fun getContactsWithBirthdays(): Flow<List<Contact>> = contactDao.getContactsWithBirthdays()

    suspend fun getContactById(id: Long): Contact? = contactDao.getContactById(id)

    suspend fun insertContact(contact: Contact) = contactDao.insertContact(contact)

    suspend fun insertContacts(contacts: List<Contact>) = contactDao.insertContacts(contacts)

    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)

    suspend fun deleteContact(contact: Contact) = contactDao.deleteContact(contact)

    suspend fun getContactsByBirthday(monthDay: String): List<Contact> =
        contactDao.getContactsByBirthday(monthDay)
}
