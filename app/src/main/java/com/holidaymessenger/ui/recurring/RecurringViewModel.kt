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
        messageRepository.getScheduledMessagesByType(MessageType.RECURRING)
            .map { messages ->
                val items = messages.map { msg ->
                    val contact = contactRepository.getContactById(msg.contactId)
                    val template = messageRepository.getTemplateById(msg.templateId)
                    RecurringMessageItem(
                        scheduledMessage = msg,
                        contactName = contact?.name ?: "Unknown",
                        templateText = template?.text ?: ""
                    )
                }
                RecurringUiState(messages = items)
            }
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
