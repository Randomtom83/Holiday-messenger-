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
            "Message Sent",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when messages are sent"
        }

        val messageFailedChannel = NotificationChannel(
            CHANNEL_MESSAGE_FAILED,
            "Message Failed",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when messages fail to send"
        }

        val schedulerChannel = NotificationChannel(
            CHANNEL_SCHEDULER,
            "Scheduler",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background scheduling notifications"
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
