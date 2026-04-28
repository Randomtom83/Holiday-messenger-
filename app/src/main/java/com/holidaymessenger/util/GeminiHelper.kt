package com.holidaymessenger.util

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.holidaymessenger.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiHelper @Inject constructor() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateHolidayMessage(holidayName: String, recipientName: String): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = "Generate a warm, friendly, and short holiday greeting for $holidayName to be sent to $recipientName. Keep it under 160 characters for SMS compatibility."
            val response = generativeModel.generateContent(prompt)
            response.text?.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateBirthdayMessage(recipientName: String): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = "Generate a warm, friendly, and short birthday greeting for $recipientName. Keep it under 160 characters for SMS compatibility."
            val response = generativeModel.generateContent(prompt)
            response.text?.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun polishMessage(text: String, holidayName: String): String? = withContext(Dispatchers.IO) {
        try {
            val holidayContext = if (holidayName.equals("Squad", ignoreCase = true)) "a general friendly" else "a festive $holidayName"
            val prompt = """
                You are a "Festive Magic" specialist. 
                Polish and improve the following message for $holidayContext: "$text"
                Make it more fun, festive, and engaging while maintaining a warm tone. 
                Maintain the "{name}" placeholder if it exists. 
                Keep the response under 160 characters for SMS compatibility.
                Only return the polished message text without quotes.
            """.trimIndent()
            val response = generativeModel.generateContent(prompt)
            response.text?.trim()?.removeSurrounding("\"")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
