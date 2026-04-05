package com.holidaymessenger.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.holidaymessenger.worker.MessageSchedulerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val schedulerRunning: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun runSchedulerNow(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<MessageSchedulerWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun ensureSchedulerRegistered(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MessageSchedulerWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            MessageSchedulerWorker.buildPeriodicRequest()
        )
    }
}
