package com.holidaymessenger.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.db.entity.*
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringEditUiState(
    val contactId: Long = 0,
    val contactName: String = "",
    val messageText: String = "",
    val frequency: Frequency = Frequency.DAILY,
    val channel: Channel = Channel.SMS,
    val windowStartMinutes: Int = 660,  // 11:00 AM
    val windowEndMinutes: Int = 840,    // 2:00 PM
    val isEditing: Boolean = false,
    val existingId: Long? = null
)

@HiltViewModel
class RecurringEditViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringEditUiState())
    val uiState: StateFlow<RecurringEditUiState> = _uiState.asStateFlow()

    fun initialize(scheduledMessageId: Long?) {
        if (scheduledMessageId == null) return

        viewModelScope.launch {
            val msg = messageRepository.getScheduledMessageById(scheduledMessageId) ?: return@launch
            val contact = contactRepository.getContactById(msg.contactId)
            val template = messageRepository.getTemplateById(msg.templateId)

            _uiState.update {
                it.copy(
                    contactId = msg.contactId,
                    contactName = contact?.name ?: "Unknown",
                    messageText = template?.text ?: "",
                    frequency = msg.frequency,
                    channel = msg.channel,
                    windowStartMinutes = msg.windowStartMinutes,
                    windowEndMinutes = msg.windowEndMinutes,
                    isEditing = true,
                    existingId = msg.id
                )
            }
        }
    }

    fun setMessageText(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun setFrequency(frequency: Frequency) {
        _uiState.update { it.copy(frequency = frequency) }
    }

    fun setChannel(channel: Channel) {
        _uiState.update { it.copy(channel = channel) }
    }

    fun setTime(hour: Int, minute: Int, isStart: Boolean) {
        val minutes = hour * 60 + minute
        _uiState.update {
            if (isStart) {
                it.copy(windowStartMinutes = minutes)
            } else {
                it.copy(windowEndMinutes = minutes)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value

            // Create or update template
            val templateId = if (state.isEditing && state.existingId != null) {
                val existing = messageRepository.getScheduledMessageById(state.existingId)
                if (existing != null) {
                    val template = messageRepository.getTemplateById(existing.templateId)
                    if (template != null) {
                        messageRepository.updateTemplate(template.copy(text = state.messageText))
                        template.id
                    } else {
                        createTemplate(state.messageText)
                    }
                } else {
                    createTemplate(state.messageText)
                }
            } else {
                createTemplate(state.messageText)
            }

            val scheduledMessage = ScheduledMessage(
                id = state.existingId ?: 0,
                contactId = state.contactId,
                templateId = templateId,
                type = MessageType.RECURRING,
                channel = state.channel,
                frequency = state.frequency,
                windowStartMinutes = state.windowStartMinutes,
                windowEndMinutes = state.windowEndMinutes,
                enabled = true
            )

            if (state.isEditing) {
                messageRepository.updateScheduledMessage(scheduledMessage)
            } else {
                messageRepository.insertScheduledMessage(scheduledMessage)
            }
        }
    }

    private suspend fun createTemplate(text: String): Long {
        return messageRepository.insertTemplate(
            MessageTemplate(
                category = Category.RECURRING,
                text = text
            )
        )
    }
}
