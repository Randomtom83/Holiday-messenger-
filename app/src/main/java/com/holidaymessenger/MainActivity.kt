package com.holidaymessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.holidaymessenger.ui.navigation.AppNavGraph
import com.holidaymessenger.ui.theme.HolidayMessengerTheme
import com.holidaymessenger.worker.MessageSchedulerWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure the daily scheduler is registered
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MessageSchedulerWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            MessageSchedulerWorker.buildPeriodicRequest()
        )

        setContent {
            HolidayMessengerTheme {
                AppNavGraph()
            }
        }
    }
}
