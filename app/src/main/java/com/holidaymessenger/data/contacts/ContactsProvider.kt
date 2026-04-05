package com.holidaymessenger.data.contacts

import android.content.Context
import android.provider.ContactsContract
import com.holidaymessenger.data.db.entity.Channel
import com.holidaymessenger.data.db.entity.Contact
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class DeviceContact(
        val id: Long,
        val name: String,
        val phoneNumber: String?,
        val birthday: String?
    )

    /**
     * Reads all contacts from the device with phone numbers.
     */
    fun getDeviceContacts(): List<DeviceContact> {
        val contacts = mutableMapOf<Long, DeviceContact>()

        // Read contacts with phone numbers
        val phoneCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        phoneCursor?.use { cursor ->
            val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                if (!contacts.containsKey(id)) {
                    contacts[id] = DeviceContact(
                        id = id,
                        name = cursor.getString(nameIdx) ?: "Unknown",
                        phoneNumber = cursor.getString(numberIdx),
                        birthday = null
                    )
                }
            }
        }

        // Read birthdays
        val birthdayCursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.START_DATE
            ),
            "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?",
            arrayOf(
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
            ),
            null
        )

        birthdayCursor?.use { cursor ->
            val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.CONTACT_ID)
            val dateIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val dateStr = cursor.getString(dateIdx)
                val monthDay = parseBirthdayToMonthDay(dateStr)
                if (monthDay != null && contacts.containsKey(id)) {
                    contacts[id] = contacts[id]!!.copy(birthday = monthDay)
                }
            }
        }

        return contacts.values.filter { it.phoneNumber != null }.toList()
    }

    /**
     * Parses various birthday date formats into "MM-dd" format.
     * Handles: "yyyy-MM-dd", "--MM-dd", "MM/dd/yyyy", "MM/dd"
     */
    fun parseBirthdayToMonthDay(dateStr: String?): String? {
        if (dateStr.isNullOrBlank()) return null

        return try {
            when {
                // Format: --MM-dd (no year)
                dateStr.startsWith("--") -> {
                    dateStr.removePrefix("--")
                }
                // Format: yyyy-MM-dd
                dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                    dateStr.substring(5)
                }
                // Format: MM/dd/yyyy
                dateStr.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) -> {
                    val parts = dateStr.split("/")
                    "${parts[0]}-${parts[1]}"
                }
                // Format: MM/dd
                dateStr.matches(Regex("\\d{2}/\\d{2}")) -> {
                    dateStr.replace("/", "-")
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reads the last SMS timestamp for each contact ID.
     * Returns a map of contact ID -> last SMS timestamp (epoch millis).
     * Requires READ_SMS permission.
     */
    fun getLastSmsTimestamps(): Map<Long, Long> {
        val timestamps = mutableMapOf<Long, Long>()

        try {
            val cursor = context.contentResolver.query(
                android.net.Uri.parse("content://sms"),
                arrayOf("person", "date"),
                null, null,
                "date DESC"
            )

            cursor?.use {
                val personIdx = it.getColumnIndex("person")
                val dateIdx = it.getColumnIndex("date")

                while (it.moveToNext()) {
                    val personId = it.getLong(personIdx)
                    val date = it.getLong(dateIdx)
                    if (personId > 0 && !timestamps.containsKey(personId)) {
                        timestamps[personId] = date
                    }
                }
            }
        } catch (e: SecurityException) {
            // READ_SMS permission not granted — return empty map
        }

        return timestamps
    }

    /**
     * Converts a DeviceContact to a Room Contact entity.
     */
    fun toContactEntity(device: DeviceContact): Contact? {
        val phone = device.phoneNumber ?: return null
        return Contact(
            id = device.id,
            name = device.name,
            phoneNumber = phone,
            birthday = device.birthday,
            preferredChannel = Channel.SMS
        )
    }
}
