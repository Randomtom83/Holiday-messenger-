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
        val birthday: String?,
        val isGroup: Boolean = false,
        val groupIds: Set<Long> = emptySet()
    )

    /**
     * Represents a Google Contacts label / group the user has created
     * (Family, Fraternity Brothers, etc.) as surfaced by Android's
     * ContactsContract.Groups table.
     */
    data class ContactGroup(
        val id: Long,
        val title: String,
        val memberCount: Int
    )

    /**
     * Reads visible, user-meaningful contact groups (labels) from the device.
     * Filters out deleted, hidden, and auto-add system groups. Groups with
     * zero members (rare, but possible for empty labels) are still returned
     * so the filter UI stays consistent.
     */
    fun getContactGroups(): List<ContactGroup> {
        val groups = mutableMapOf<Long, Pair<String, Int>>() // id -> (title, count)

        try {
            val groupsCursor = context.contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                arrayOf(
                    ContactsContract.Groups._ID,
                    ContactsContract.Groups.TITLE,
                    ContactsContract.Groups.DELETED,
                    ContactsContract.Groups.AUTO_ADD,
                    ContactsContract.Groups.FAVORITES
                ),
                // Non-deleted, non-system groups. We deliberately do NOT filter on
                // GROUP_VISIBLE because Google Contacts leaves user-created labels
                // with group_visible=0 — only legacy system groups ("My Contacts",
                // "Starred") get group_visible=1, and we exclude those via
                // AUTO_ADD / FAVORITES.
                "${ContactsContract.Groups.DELETED} = 0 AND " +
                    "${ContactsContract.Groups.AUTO_ADD} = 0 AND " +
                    "${ContactsContract.Groups.FAVORITES} = 0",
                null,
                "${ContactsContract.Groups.TITLE} ASC"
            )

            groupsCursor?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.Groups._ID)
                val titleIdx = cursor.getColumnIndex(ContactsContract.Groups.TITLE)
                if (idIdx != -1 && titleIdx != -1) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIdx)
                        val title = cursor.getString(titleIdx)
                        if (!title.isNullOrBlank()) {
                            // Use the latest title if the group appears twice (multi-account)
                            val existing = groups[id]
                            groups[id] = (title to (existing?.second ?: 0))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsProvider", "Error loading contact groups", e)
            return emptyList()
        }

        // Count distinct contacts per group (dedupe across multi-account raw contacts)
        try {
            val membershipCursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                ),
                "${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE),
                null
            )

            membershipCursor?.use { cursor ->
                val contactIdIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val groupIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)
                if (contactIdIdx != -1 && groupIdIdx != -1) {
                    val uniqueMembers = mutableMapOf<Long, MutableSet<Long>>()
                    while (cursor.moveToNext()) {
                        val groupId = cursor.getLong(groupIdIdx)
                        val contactId = cursor.getLong(contactIdIdx)
                        if (groups.containsKey(groupId)) {
                            uniqueMembers.getOrPut(groupId) { mutableSetOf() }.add(contactId)
                        }
                    }
                    uniqueMembers.forEach { (id, members) ->
                        groups[id]?.let { (title, _) ->
                            groups[id] = title to members.size
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsProvider", "Error counting group members", e)
        }

        return groups.map { (id, pair) ->
            ContactGroup(id = id, title = pair.first, memberCount = pair.second)
        }.filter { it.memberCount > 0 } // Hide empty labels
            .sortedBy { it.title.lowercase() }
    }

    /**
     * Reads all contacts from the device with phone numbers, including groups.
     */
    fun getDeviceContacts(): List<DeviceContact> {
        val contacts = mutableMapOf<Long, DeviceContact>()

        // Read contacts with phone numbers
        try {
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

                if (idIdx != -1 && nameIdx != -1 && numberIdx != -1) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIdx)
                        if (!contacts.containsKey(id)) {
                            contacts[id] = DeviceContact(
                                id = id,
                                name = cursor.getString(nameIdx) ?: "Unknown",
                                phoneNumber = cursor.getString(numberIdx),
                                birthday = null,
                                isGroup = false
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsProvider", "Error loading phone contacts", e)
        }

        // Read Group Chats from SMS threads
        try {
            val threadUri = android.net.Uri.parse("content://mms-sms/conversations?simple=true")
            val threadCursor = context.contentResolver.query(
                threadUri,
                arrayOf("_id", "recipient_ids"),
                null, null, "date DESC"
            )

            threadCursor?.use { cursor ->
                val threadIdIdx = cursor.getColumnIndex("_id")
                val recipientIdsIdx = cursor.getColumnIndex("recipient_ids")
                
                if (threadIdIdx != -1 && recipientIdsIdx != -1) {
                    while (cursor.moveToNext()) {
                        val threadId = cursor.getLong(threadIdIdx)
                        val recipientIds = cursor.getString(recipientIdsIdx) ?: ""
                        
                        if (recipientIds.split(" ").size > 1) {
                            // This is a group thread
                            val recipientsUri = android.net.Uri.parse("content://mms-sms/threadID/$threadId/recipients")
                            val addrCursor = try {
                                context.contentResolver.query(
                                    recipientsUri,
                                    arrayOf("address", "display_name"),
                                    null, null, null
                                )
                            } catch (e: Exception) { null }
                            
                            addrCursor?.use { ac ->
                                val gNameIdx = ac.getColumnIndex("display_name")
                                val gAddrIdx = ac.getColumnIndex("address")
                                
                                val names = mutableListOf<String>()
                                val numbers = mutableListOf<String>()
                                while (ac.moveToNext()) {
                                    val name = try { if (gNameIdx != -1) ac.getString(gNameIdx) else null } catch (e: Exception) { null }
                                    val addr = try { if (gAddrIdx != -1) ac.getString(gAddrIdx) else null } catch (e: Exception) { null }
                                    if (!name.isNullOrBlank()) names.add(name)
                                    if (!addr.isNullOrBlank()) numbers.add(addr)
                                }
                                
                                if (numbers.isNotEmpty()) {
                                    val groupName = if (names.isNotEmpty()) {
                                        "Group: ${names.take(3).joinToString(", ")}${if (names.size > 3) "..." else ""}"
                                    } else {
                                        "Group Chat #$threadId"
                                    }
                                    
                                    val groupId = -threadId // Use negative ID to distinguish from contacts
                                    
                                    if (!contacts.containsKey(groupId)) {
                                        contacts[groupId] = DeviceContact(
                                            id = groupId,
                                            name = groupName,
                                            phoneNumber = numbers.joinToString(","),
                                            birthday = null,
                                            isGroup = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsProvider", "Error loading groups", e)
        }

        // Read group memberships (Google Contacts labels: Family, Fraternity, etc.)
        try {
            val membershipCursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                ),
                "${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE),
                null
            )

            membershipCursor?.use { cursor ->
                val contactIdIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val groupIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)
                if (contactIdIdx != -1 && groupIdIdx != -1) {
                    while (cursor.moveToNext()) {
                        val contactId = cursor.getLong(contactIdIdx)
                        val groupId = cursor.getLong(groupIdIdx)
                        contacts[contactId]?.let { existing ->
                            contacts[contactId] = existing.copy(groupIds = existing.groupIds + groupId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsProvider", "Error loading group memberships", e)
        }

        // Read birthdays
        try {
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

                if (idIdx != -1 && dateIdx != -1) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIdx)
                        val dateStr = cursor.getString(dateIdx)
                        val monthDay = parseBirthdayToMonthDay(dateStr)
                        if (monthDay != null && contacts.containsKey(id)) {
                            contacts[id] = contacts[id]!!.copy(birthday = monthDay)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsProvider", "Error loading birthdays", e)
        }

        return contacts.values.filter { it.phoneNumber != null }.toList()
    }

    /**
     * Parses various birthday date formats into "MM-dd" format.
     * Handles: "yyyy-MM-dd", "--MM-dd", "MM/dd/yyyy", "MM/dd", "dd/MM/yyyy", "dd/MM"
     * And various delimiters: "-", "/", "."
     */
    fun parseBirthdayToMonthDay(dateStr: String?): String? {
        if (dateStr.isNullOrBlank()) return null

        val cleaned = dateStr.replace("/", "-").replace(".", "-").trim()

        return try {
            when {
                // Format: --MM-dd (no year)
                cleaned.startsWith("--") -> {
                    cleaned.removePrefix("--")
                }
                // Format: yyyy-MM-dd
                cleaned.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                    val parts = cleaned.split("-")
                    "${parts[1]}-${parts[2]}"
                }
                // Format: MM-dd-yyyy or dd-MM-yyyy
                cleaned.matches(Regex("\\d{2}-\\d{2}-\\d{4}")) -> {
                    val parts = cleaned.split("-")
                    val p1 = parts[0].toInt()
                    // If p1 > 12, it must be dd-MM-yyyy
                    if (p1 > 12) {
                        "${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                    } else {
                        // Ambiguous, assume MM-dd-yyyy (common for Android)
                        "${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}"
                    }
                }
                // Format: MM-dd or dd-MM
                cleaned.matches(Regex("\\d{2}-\\d{2}")) -> {
                    val parts = cleaned.split("-")
                    val p1 = parts[0].toInt()
                    if (p1 > 12) {
                        "${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                    } else {
                        "${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}"
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reads the message count and last timestamp for each contact/thread.
     */
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
