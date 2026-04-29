package com.holidaymessenger.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.contacts.ContactsProvider
import com.holidaymessenger.data.db.entity.Contact
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.HolidayRepository
import com.holidaymessenger.messaging.SmsSender
import com.holidaymessenger.util.GeminiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ContactSortOrder {
    ALPHABETICAL,
    SELECTED
}

data class ContactPickerUiState(
    val deviceContacts: List<ContactsProvider.DeviceContact> = emptyList(),
    val selectedContactIds: Set<Long> = emptySet(),
    val squadContactIds: Set<Long> = emptySet(),
    val savedContacts: List<Contact> = emptyList(),
    val contactGroups: List<ContactsProvider.ContactGroup> = emptyList(),
    val selectedGroupIds: Set<Long> = emptySet(),
    val sortOrder: ContactSortOrder = ContactSortOrder.ALPHABETICAL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showBlastDialog: Boolean = false,
    val blastMessage: String = "",
    val isSending: Boolean = false,
    val isPolishing: Boolean = false,
    val showConfetti: Boolean = false,
    val showLateMessageWarning: Boolean = false,
    val lateHolidayName: String? = null
)

@HiltViewModel
class ContactPickerViewModel @Inject constructor(
    private val contactsProvider: ContactsProvider,
    private val contactRepository: ContactRepository,
    private val holidayRepository: HolidayRepository,
    private val smsSender: SmsSender,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactPickerUiState())
    val uiState: StateFlow<ContactPickerUiState> = _uiState.asStateFlow()

    private var holidayId: Long? = null

    fun initialize(holidayId: Long?) {
        this.holidayId = holidayId
        loadContacts()

        // Observe saved contacts reactively
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { saved ->
                _uiState.update { it.copy(savedContacts = saved) }
            }
        }

        // Always track squad membership so it can act as a sort tiebreaker on
        // holiday pickers (squad members float above strangers even before any
        // contacts have been assigned to the specific holiday).
        viewModelScope.launch {
            holidayRepository.getContactIdsForHoliday(0).collect { ids ->
                _uiState.update { it.copy(squadContactIds = ids.toSet()) }
            }
        }

        if (holidayId != null && holidayId > 0) {
            viewModelScope.launch {
                val holiday = holidayRepository.getHolidayById(holidayId)
                if (holiday != null) {
                    checkIfHolidayIsPast(holiday)
                }

                holidayRepository.getContactIdsForHoliday(holidayId).collect { ids ->
                    _uiState.update { it.copy(selectedContactIds = ids.toSet()) }
                }
            }
        } else if (holidayId == 0L || holidayId == null) {
            // "The Squad" (holidayId = 0) — selection IS squad membership
            viewModelScope.launch {
                holidayRepository.getContactIdsForHoliday(0).collect { ids ->
                    _uiState.update { it.copy(selectedContactIds = ids.toSet()) }
                }
            }
        }
    }

    private fun checkIfHolidayIsPast(holiday: Holiday) {
        val today = java.time.LocalDate.now()
        val holidayDate: java.time.LocalDate? = if (holiday.isVariable) {
            com.holidaymessenger.util.HolidayCalendar.getHolidaysForYear(today.year)
                .find { it.name == holiday.name }?.date
        } else {
            holiday.monthDay?.let { md ->
                val parts = md.split("-")
                if (parts.size == 2) {
                    java.time.LocalDate.of(today.year, parts[0].toInt(), parts[1].toInt())
                } else null
            }
        }

        if (holidayDate != null && holidayDate.isBefore(today)) {
            _uiState.update { it.copy(showLateMessageWarning = true, lateHolidayName = holiday.name) }
        }
    }

    fun dismissLateWarning() {
        _uiState.update { it.copy(showLateMessageWarning = false) }
    }

    private fun loadContacts() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val contacts = contactsProvider.getDeviceContacts()
            val groups = contactsProvider.getContactGroups()

            _uiState.update {
                it.copy(
                    deviceContacts = sortContacts(contacts, it.sortOrder),
                    contactGroups = groups,
                    isLoading = false
                )
            }
        }
    }

    fun toggleGroupFilter(groupId: Long) {
        _uiState.update { state ->
            val next = if (state.selectedGroupIds.contains(groupId)) {
                state.selectedGroupIds - groupId
            } else {
                state.selectedGroupIds + groupId
            }
            state.copy(selectedGroupIds = next)
        }
    }

    fun clearGroupFilter() {
        _uiState.update { it.copy(selectedGroupIds = emptySet()) }
    }

    /**
     * Selects every contact currently visible in the filtered view
     * (respects both search query and group filter). Saves them to the DB
     * and links them to the current holiday / Squad.
     */
    fun selectAllFiltered() {
        viewModelScope.launch {
            val visible = getFilteredContacts()
            if (visible.isEmpty()) return@launch
            val effectiveHolidayId = holidayId ?: 0L
            val currentSelected = _uiState.value.selectedContactIds
            val toAdd = visible.filter { !currentSelected.contains(it.id) }

            for (contact in toAdd) {
                val existing = contactRepository.getContactById(contact.id)
                if (existing == null) {
                    val entity = contactsProvider.toContactEntity(contact)
                    if (entity != null) {
                        contactRepository.insertContact(entity)
                    }
                }
                holidayRepository.addContactToHoliday(effectiveHolidayId, contact.id)
            }

            _uiState.update { state ->
                state.copy(selectedContactIds = state.selectedContactIds + toAdd.map { it.id })
            }
        }
    }

    fun setSortOrder(order: ContactSortOrder) {
        _uiState.update {
            it.copy(
                sortOrder = order,
                deviceContacts = sortContacts(it.deviceContacts, order)
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleContact(contactId: Long) {
        viewModelScope.launch {
            val current = _uiState.value.selectedContactIds
            val deviceContact = _uiState.value.deviceContacts.find { it.id == contactId }

            val effectiveHolidayId = holidayId ?: 0L

            val newSelectedIds = if (current.contains(contactId)) {
                // Remove
                holidayRepository.removeContactFromHoliday(effectiveHolidayId, contactId)
                current - contactId
            } else {
                // Add — also save to DB if not already there
                if (deviceContact != null) {
                    val existing = contactRepository.getContactById(deviceContact.id)
                    if (existing == null) {
                        val entity = contactsProvider.toContactEntity(deviceContact)
                        if (entity != null) {
                            contactRepository.insertContact(entity)
                        }
                    }
                }
                holidayRepository.addContactToHoliday(effectiveHolidayId, contactId)
                current + contactId
            }

            _uiState.update { state ->
                val updatedDeviceContacts = if (state.sortOrder == ContactSortOrder.SELECTED) {
                    state.deviceContacts.sortedByDescending { newSelectedIds.contains(it.id) }
                } else {
                    state.deviceContacts
                }
                state.copy(
                    selectedContactIds = newSelectedIds,
                    deviceContacts = updatedDeviceContacts
                )
            }
        }
    }

    fun updateContactChannel(contactId: Long, channel: com.holidaymessenger.data.db.entity.Channel) {
        viewModelScope.launch {
            val contact = contactRepository.getContactById(contactId)
            if (contact != null) {
                contactRepository.updateContact(contact.copy(preferredChannel = channel))
            }
        }
    }

    fun clearAllContacts() {
        viewModelScope.launch {
            val effectiveHolidayId = holidayId ?: 0L
            holidayRepository.clearAllContactsForHoliday(effectiveHolidayId)
            _uiState.update { it.copy(selectedContactIds = emptySet()) }
        }
    }

    fun applyToAllHolidays() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedContactIds.toList()
            if (selectedIds.isEmpty()) return@launch

            val effectiveHolidayId = holidayId ?: 0L
            val enabledHolidays = holidayRepository.getEnabledHolidays()
            val holidayIds = enabledHolidays.map { it.id }.toMutableList()
            
            // Remove the source holiday from target list
            holidayIds.remove(effectiveHolidayId)
            
            // If the source is a holiday, we should also sync to the Squad (id=0) 
            // unless the source IS the squad.
            if (effectiveHolidayId != 0L && !holidayIds.contains(0L)) {
                holidayIds.add(0L)
            }
            
            holidayRepository.overwriteContactsInHolidays(holidayIds, selectedIds)
            _uiState.update { it.copy(showConfetti = true) }
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(showConfetti = false) }
        }
    }

    fun openBlastDialog() {
        viewModelScope.launch {
            val defaultMessage = "Hey {name}, just thinking of you! Hope you're having a magical day! ✨"
            val message = if (holidayId != null && holidayId!! > 0) {
                holidayRepository.getTemplateForHoliday(holidayId!!)?.text ?: defaultMessage
            } else {
                defaultMessage
            }
            _uiState.update { it.copy(showBlastDialog = true, blastMessage = message) }
        }
    }

    fun closeBlastDialog() {
        _uiState.update { it.copy(showBlastDialog = false) }
    }

    fun updateBlastMessage(message: String) {
        _uiState.update { it.copy(blastMessage = message) }
    }

    fun polishBlastMessage() {
        val currentMessage = _uiState.value.blastMessage
        if (currentMessage.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPolishing = true) }
            val polished = geminiHelper.polishMessage(currentMessage, "General Joy")
            _uiState.update { it.copy(blastMessage = polished ?: currentMessage, isPolishing = false) }
        }
    }

    fun sendBlast() {
        val state = _uiState.value
        val selectedIds = state.selectedContactIds
        val messageTemplate = state.blastMessage

        if (selectedIds.isEmpty() || messageTemplate.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            val contactsToBlast = state.deviceContacts.filter { selectedIds.contains(it.id) }
            
            for (contact in contactsToBlast) {
                val personalized = messageTemplate.replace("{name}", contact.name)
                val savedContact = state.savedContacts.find { it.id == contact.id }
                val channel = savedContact?.preferredChannel ?: com.holidaymessenger.data.db.entity.Channel.SMS
                
                contact.phoneNumber?.let { num ->
                    smsSender.send(num, personalized, channel)
                }
            }

            _uiState.update { it.copy(isSending = false, showBlastDialog = false, showConfetti = true) }
            kotlinx.coroutines.delay(4000)
            _uiState.update { it.copy(showConfetti = false) }
        }
    }

    private fun sortContacts(
        contacts: List<ContactsProvider.DeviceContact>,
        order: ContactSortOrder
    ): List<ContactsProvider.DeviceContact> {
        return when (order) {
            ContactSortOrder.ALPHABETICAL -> contacts.sortedBy { it.name.lowercase() }
            ContactSortOrder.SELECTED -> {
                val state = _uiState.value
                val selected = state.selectedContactIds
                val squad = state.squadContactIds
                contacts.sortedWith(
                    compareByDescending<ContactsProvider.DeviceContact> { selected.contains(it.id) }
                        .thenByDescending { squad.contains(it.id) }
                        .thenBy { it.name.lowercase() }
                )
            }
        }
    }

    fun getFilteredContacts(): List<ContactsProvider.DeviceContact> {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val activeGroups = state.selectedGroupIds

        return state.deviceContacts.filter { contact ->
            val matchesQuery = query.isEmpty() ||
                contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumber?.contains(query) == true

            // When any group filter is active, only contacts that belong to
            // at least one of the selected groups pass. SMS group chats
            // (which have no Google groups) are hidden while a filter is on.
            val matchesGroup = activeGroups.isEmpty() ||
                contact.groupIds.any { it in activeGroups }

            matchesQuery && matchesGroup
        }
    }
}
