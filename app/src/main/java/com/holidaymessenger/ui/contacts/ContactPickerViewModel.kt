package com.holidaymessenger.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.contacts.ContactsProvider
import com.holidaymessenger.data.db.entity.Contact
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.HolidayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ContactSortOrder {
    ALPHABETICAL,
    RECENTLY_TEXTED,
    OLDEST_TEXTED
}

data class ContactPickerUiState(
    val deviceContacts: List<ContactsProvider.DeviceContact> = emptyList(),
    val selectedContactIds: Set<Long> = emptySet(),
    val savedContacts: List<Contact> = emptyList(),
    val sortOrder: ContactSortOrder = ContactSortOrder.RECENTLY_TEXTED,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ContactPickerViewModel @Inject constructor(
    private val contactsProvider: ContactsProvider,
    private val contactRepository: ContactRepository,
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactPickerUiState())
    val uiState: StateFlow<ContactPickerUiState> = _uiState.asStateFlow()

    private var holidayId: Long? = null
    private var smsTimestamps: Map<Long, Long> = emptyMap()

    fun initialize(holidayId: Long?) {
        this.holidayId = holidayId
        loadContacts()

        if (holidayId != null && holidayId > 0) {
            viewModelScope.launch {
                holidayRepository.getContactIdsForHoliday(holidayId).collect { ids ->
                    _uiState.update { it.copy(selectedContactIds = ids.toSet()) }
                }
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val contacts = contactsProvider.getDeviceContacts()
            smsTimestamps = try {
                contactsProvider.getLastSmsTimestamps()
            } catch (e: Exception) {
                emptyMap()
            }

            _uiState.update {
                it.copy(
                    deviceContacts = sortContacts(contacts, it.sortOrder),
                    isLoading = false
                )
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

            if (current.contains(contactId)) {
                // Remove
                _uiState.update { it.copy(selectedContactIds = current - contactId) }
                holidayId?.let { hid ->
                    holidayRepository.removeContactFromHoliday(hid, contactId)
                }
            } else {
                // Add — also save to DB if not already there
                _uiState.update { it.copy(selectedContactIds = current + contactId) }
                if (deviceContact != null) {
                    val entity = contactsProvider.toContactEntity(deviceContact)
                    if (entity != null) {
                        contactRepository.insertContact(entity)
                    }
                }
                holidayId?.let { hid ->
                    holidayRepository.addContactToHoliday(hid, contactId)
                }
            }
        }
    }

    private fun sortContacts(
        contacts: List<ContactsProvider.DeviceContact>,
        order: ContactSortOrder
    ): List<ContactsProvider.DeviceContact> {
        return when (order) {
            ContactSortOrder.ALPHABETICAL -> contacts.sortedBy { it.name.lowercase() }
            ContactSortOrder.RECENTLY_TEXTED -> contacts.sortedByDescending {
                smsTimestamps[it.id] ?: 0L
            }
            ContactSortOrder.OLDEST_TEXTED -> contacts.sortedBy {
                smsTimestamps[it.id] ?: Long.MAX_VALUE
            }
        }
    }

    fun getFilteredContacts(): List<ContactsProvider.DeviceContact> {
        val state = _uiState.value
        return if (state.searchQuery.isBlank()) {
            state.deviceContacts
        } else {
            state.deviceContacts.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                    it.phoneNumber?.contains(state.searchQuery) == true
            }
        }
    }
}
