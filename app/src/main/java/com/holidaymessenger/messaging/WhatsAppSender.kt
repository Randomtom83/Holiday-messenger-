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
     * Opens WhatsApp with a pre-filled message to the given phone number.
     * Note: This cannot fully auto-send — the user will need to tap send in WhatsApp.
     * This is a limitation of WhatsApp's consumer API.
     *
     * @param phoneNumber Phone number with country code (e.g., "+1234567890")
     * @param message The message text to pre-fill
     * @return true if WhatsApp was opened, false if WhatsApp is not installed
     */
    fun send(phoneNumber: String, message: String): Boolean {
        return try {
            val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
            val uri = Uri.parse(
                "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
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
