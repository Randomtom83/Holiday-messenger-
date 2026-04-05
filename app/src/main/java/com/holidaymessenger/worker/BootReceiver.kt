package com.holidaymessenger.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager

/**
 * Re-registers the daily scheduler after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                MessageSchedulerWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                MessageSchedulerWorker.buildPeriodicRequest()
            )
        }
    }
}
