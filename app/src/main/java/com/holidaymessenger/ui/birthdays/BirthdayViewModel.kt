package com.holidaymessenger.ui.birthdays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.db.entity.*
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BirthdayContact(
    val contact: Contact,
    val scheduledMessage: ScheduledMessage?,
    val isEnabled: Boolean
)

data class BirthdayUiState(
    val birthdayContacts: List<BirthdayContact> = emptyList(),
    val editingContact: Contact? = null,
    val showBirthdayDialog: Boolean = false,
    val birthdayInput: String = "",
    val showTemplateDialog: Boolean = false,
    val templateInput: String = ""
)

@HiltViewModel
class BirthdayViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BirthdayUiState())
    val uiState: StateFlow<BirthdayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                contactRepository.getContactsWithBirthdays(),
                messageRepository.getScheduledMessagesByType(MessageType.BIRTHDAY)
            ) { contacts, scheduled ->
                contacts.map { contact ->
                    val msg = scheduled.find { it.contactId == contact.id }
                    BirthdayContact(
                        contact = contact,
                        scheduledMessage = msg,
                        isEnabled = msg?.enabled ?: false
                    )
                }
            }.collect { birthdayContacts ->
                _uiState.update { it.copy(birthdayContacts = birthdayContacts) }
            }
        }
    }

    fun toggleBirthdayMessage(birthdayContact: BirthdayContact) {
        viewModelScope.launch {
            val msg = birthdayContact.scheduledMessage
            if (msg != null) {
                messageRepository.setEnabled(msg.id, !msg.enabled)
            } else {
                // Create a new birthday scheduled message
                val templateId = birthdayContact.contact.birthdayTemplateId ?: run {
                    val template = messageRepository.getFirstTemplateByCategory(Category.BIRTHDAY)
                    template?.id ?: messageRepository.insertTemplate(
                        MessageTemplate(
                            category = Category.BIRTHDAY,
                            text = "Happy Birthday, {name}! Hope you have an amazing day!"
                        )
                    )
                }

                messageRepository.insertScheduledMessage(
                    ScheduledMessage(
                        contactId = birthdayContact.contact.id,
                        templateId = templateId,
                        type = MessageType.BIRTHDAY,
                        channel = birthdayContact.contact.preferredChannel,
                        frequency = Frequency.YEARLY,
                        windowStartMinutes = 540, // 9:00 AM
                        windowEndMinutes = 660,    // 11:00 AM
                        enabled = true
                    )
                )
            }
        }
    }

    fun editBirthday(contact: Contact) {
        _uiState.update {
            it.copy(
                editingContact = contact,
                showBirthdayDialog = true,
                birthdayInput = contact.effectiveBirthday ?: ""
            )
        }
    }

    fun saveBirthday(monthDay: String) {
        val contact = _uiState.value.editingContact ?: return
        viewModelScope.launch {
            contactRepository.updateContact(
                contact.copy(birthdayOverride = monthDay.ifBlank { null })
            )
            _uiState.update { it.copy(showBirthdayDialog = false) }
        }
    }

    fun editTemplate(contact: Contact) {
        viewModelScope.launch {
            val templateId = contact.birthdayTemplateId
            val templateText = if (templateId != null) {
                messageRepository.getTemplateById(templateId)?.text ?: ""
            } else {
                messageRepository.getFirstTemplateByCategory(Category.BIRTHDAY)?.text ?: ""
            }

            _uiState.update {
                it.copy(
                    editingContact = contact,
                    showTemplateDialog = true,
                    templateInput = templateText
                )
            }
        }
    }

    fun saveTemplate(text: String) {
        val contact = _uiState.value.editingContact ?: return
        viewModelScope.launch {
            val newTemplateId = messageRepository.insertTemplate(
                MessageTemplate(
                    category = Category.BIRTHDAY,
                    text = text
                )
            )

            // Update contact to use this specific template
            contactRepository.updateContact(
                contact.copy(birthdayTemplateId = newTemplateId)
            )

            // Update any existing scheduled birthday message for this contact
            val birthdayMessages = messageRepository.getScheduledMessagesByType(MessageType.BIRTHDAY).first()
            val existingMsg = birthdayMessages.find { it.contactId == contact.id }
            
            if (existingMsg != null) {
                messageRepository.updateScheduledMessage(
                    existingMsg.copy(templateId = newTemplateId)
                )
            }

            _uiState.update { it.copy(showTemplateDialog = false) }
        }
    }

    fun dismissBirthdayDialog() {
        _uiState.update { it.copy(showBirthdayDialog = false) }
    }

    fun dismissTemplateDialog() {
        _uiState.update { it.copy(showTemplateDialog = false) }
    }
}
