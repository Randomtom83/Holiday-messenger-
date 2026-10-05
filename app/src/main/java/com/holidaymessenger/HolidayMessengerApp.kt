package com.holidaymessenger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HolidayMessengerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val messageSentChannel = NotificationChannel(
            CHANNEL_MESSAGE_SENT,
            "Magic Delivered! ✨",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when festive joy is successfully spread"
            enableLights(true)
            lightColor = 0xFF2E7D32.toInt() // Festive Green
        }

        val messageFailedChannel = NotificationChannel(
            CHANNEL_MESSAGE_FAILED,
            "Festive Fumbles 😵",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when the magic hits a snag"
            enableLights(true)
            lightColor = 0xFFC62828.toInt() // Festive Red
            enableVibration(true)
        }

        val schedulerChannel = NotificationChannel(
            CHANNEL_SCHEDULER,
            "Magic Workshop 🛠️",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background holiday preparations"
        }

        manager.createNotificationChannels(
            listOf(messageSentChannel, messageFailedChannel, schedulerChannel)
        )
    }

    companion object {
        const val CHANNEL_MESSAGE_SENT = "message_sent"
        const val CHANNEL_MESSAGE_FAILED = "message_failed"
        const val CHANNEL_SCHEDULER = "scheduler"
    }
}
