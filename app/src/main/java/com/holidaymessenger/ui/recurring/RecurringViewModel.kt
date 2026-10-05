package com.holidaymessenger.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.db.entity.*
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringMessageItemInternal(
    val id: Long,
    val contactId: Long,
    val templateId: Long,
    val type: MessageType,
    val channel: Channel,
    val frequency: Frequency,
    val windowStartMinutes: Int,
    val windowEndMinutes: Int,
    val enabled: Boolean,
    val lastSentDate: String?,
    val nextScheduledTime: Long?,
    val lastScheduledDate: String?,
    val holidayId: Long?,
    val contactName: String,
    val templateText: String
)

data class RecurringMessageItem(
    val scheduledMessage: ScheduledMessage,
    val contactName: String,
    val templateText: String
)

data class RecurringUiState(
    val messages: List<RecurringMessageItem> = emptyList()
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    val uiState: StateFlow<RecurringUiState> =
        messageRepository.getRecurringMessageItems()
            .map { items -> RecurringUiState(messages = items) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecurringUiState())

    fun toggleEnabled(message: ScheduledMessage) {
        viewModelScope.launch {
            messageRepository.setEnabled(message.id, !message.enabled)
        }
    }

    fun deleteMessage(message: ScheduledMessage) {
        viewModelScope.launch {
            messageRepository.deleteScheduledMessage(message)
        }
    }
}
