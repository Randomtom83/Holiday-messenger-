package com.holidaymessenger.messaging

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppSender @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Creates an intent to open WhatsApp with a pre-filled message.
     */
    fun getWhatsAppIntent(phoneNumber: String, message: String): Intent? {
        return try {
            val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
            val uri = Uri.parse(
                "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}"
            )
            Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } catch (e: Exception) {
            null
        }
    }

    @Deprecated("Background start is blocked on Android 10+", ReplaceWith("getWhatsAppIntent(phoneNumber, message)"))
    fun send(phoneNumber: String, message: String): Boolean {
        val intent = getWhatsAppIntent(phoneNumber, message) ?: return false
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if WhatsApp is installed on the device.
     */
    fun isWhatsAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
