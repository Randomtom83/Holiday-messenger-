package com.holidaymessenger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.db.entity.MessageLog
import com.holidaymessenger.data.db.entity.ScheduledMessage
import com.holidaymessenger.data.repository.HolidayRepository
import com.holidaymessenger.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentMessages: List<MessageLog> = emptyList(),
    val upcomingMessages: List<ScheduledMessage> = emptyList(),
    val totalScheduled: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        messageRepository.getRecentLogs(10),
        messageRepository.getAllScheduledMessages()
    ) { logs, scheduled ->
        HomeUiState(
            recentMessages = logs,
            upcomingMessages = scheduled.filter { it.enabled },
            totalScheduled = scheduled.count { it.enabled }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch {
            holidayRepository.seedDefaultHolidays()
        }
    }
}
