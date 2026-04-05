package com.holidaymessenger.ui.holidays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.data.db.entity.MessageTemplate
import com.holidaymessenger.data.repository.HolidayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HolidayUiState(
    val holidays: List<Holiday> = emptyList(),
    val selectedHoliday: Holiday? = null,
    val editingTemplate: MessageTemplate? = null,
    val showTemplateDialog: Boolean = false
)

@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HolidayUiState())
    val uiState: StateFlow<HolidayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            holidayRepository.getAllHolidays().collect { holidays ->
                _uiState.update { it.copy(holidays = holidays) }
            }
        }
    }

    fun toggleHoliday(holiday: Holiday) {
        viewModelScope.launch {
            holidayRepository.updateHoliday(holiday.copy(enabled = !holiday.enabled))
        }
    }

    fun editTemplate(holiday: Holiday) {
        viewModelScope.launch {
            val template = holidayRepository.getTemplateForHoliday(holiday.id)
            _uiState.update {
                it.copy(
                    selectedHoliday = holiday,
                    editingTemplate = template,
                    showTemplateDialog = true
                )
            }
        }
    }

    fun saveTemplate(text: String) {
        val holiday = _uiState.value.selectedHoliday ?: return
        viewModelScope.launch {
            holidayRepository.saveTemplateForHoliday(holiday.id, text)
            _uiState.update { it.copy(showTemplateDialog = false) }
        }
    }

    fun dismissTemplateDialog() {
        _uiState.update { it.copy(showTemplateDialog = false) }
    }
}
