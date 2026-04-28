package com.holidaymessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.holidaymessenger.ui.navigation.AppNavGraph
import com.holidaymessenger.ui.theme.HolidayMessengerTheme
import com.holidaymessenger.worker.BirthdayCheckWorker
import com.holidaymessenger.worker.MessageSchedulerWorker
import dagger.hilt.android.AndroidEntryPoint
import androidx.work.PeriodicWorkRequest
import java.util.concurrent.TimeUnit
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions results if necessary
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request necessary permissions at startup
        requestInitialPermissions()

        // Ensure the daily scheduler is registered
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MessageSchedulerWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            MessageSchedulerWorker.buildPeriodicRequest()
        )

        // Ensure birthday sync is registered
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            BirthdayCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            BirthdayCheckWorker.buildPeriodicRequest()
        )

        setContent {
            HolidayMessengerTheme {
                AppNavGraph()
            }
        }
    }

    private fun requestInitialPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestPermissionsLauncher.launch(permissions.toTypedArray())
    }
}
