package com.holidaymessenger.messaging

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSender @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Sends an SMS or MMS message. 
     * If the phoneNumber contains commas, it's treated as a group.
     * @return true if sent successfully, false on failure
     */
    fun send(phoneNumber: String, message: String, channel: com.holidaymessenger.data.db.entity.Channel = com.holidaymessenger.data.db.entity.Channel.SMS): Boolean {
        return try {
            when (channel) {
                com.holidaymessenger.data.db.entity.Channel.SMS -> sendSms(phoneNumber, message)
                com.holidaymessenger.data.db.entity.Channel.WHATSAPP -> sendWhatsApp(phoneNumber, message)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager = context.getSystemService(SmsManager::class.java)
        if (phoneNumber.contains(",")) {
            sendGroupMms(phoneNumber, message)
        } else {
            if (message.length > 160) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
        }
    }

    private fun sendWhatsApp(phoneNumber: String, message: String) {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun sendGroupMms(phoneNumbers: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumbers")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
