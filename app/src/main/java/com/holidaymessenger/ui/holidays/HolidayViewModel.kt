package com.holidaymessenger.ui.holidays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.data.db.entity.MessageTemplate
import com.holidaymessenger.data.repository.HolidayRepository
import com.holidaymessenger.util.GeminiHelper
import com.holidaymessenger.util.HolidayCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HolidayUiState(
    val holidays: List<Holiday> = emptyList(),
    val selectedHoliday: Holiday? = null,
    val editingTemplate: MessageTemplate? = null,
    val showTemplateDialog: Boolean = false,
    val isPolishing: Boolean = false,
    val showConfetti: Boolean = false,
    // Add/edit-holiday dialog. editingHoliday=null + showAddEditDialog=true means "adding new".
    val showAddEditDialog: Boolean = false,
    val editingHoliday: Holiday? = null
)

/** Holidays with id <= this are seeded defaults — they cannot be deleted. */
const val LAST_SEEDED_HOLIDAY_ID = 36L

@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val holidayRepository: HolidayRepository,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HolidayUiState())
    val uiState: StateFlow<HolidayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            holidayRepository.getAllHolidays().collect { holidays ->
                _uiState.update { it.copy(holidays = sortByNextOccurrence(holidays)) }
            }
        }
    }

    /**
     * Sort holidays by closest upcoming date so the next holiday floats to the top.
     * Built-in holidays use HolidayCalendar (handles variable dates like Easter,
     * Memorial Day, etc.). User-added fixed-date holidays are computed from monthDay.
     * Squad-style entries with no date sort to the end.
     */
    private fun sortByNextOccurrence(holidays: List<Holiday>): List<Holiday> {
        val today = LocalDate.now()
        val thisYear = HolidayCalendar.getHolidaysForYear(today.year).associateBy { it.name }
        val nextYear = HolidayCalendar.getHolidaysForYear(today.year + 1).associateBy { it.name }

        return holidays.sortedBy { h ->
            nextOccurrence(h, today, thisYear, nextYear) ?: LocalDate.MAX
        }
    }

    private fun nextOccurrence(
        h: Holiday,
        today: LocalDate,
        thisYear: Map<String, HolidayCalendar.HolidayDate>,
        nextYear: Map<String, HolidayCalendar.HolidayDate>
    ): LocalDate? {
        thisYear[h.name]?.date?.let { if (!it.isBefore(today)) return it }
        nextYear[h.name]?.date?.let { return it }

        val md = h.monthDay ?: return null
        val parts = md.split("-")
        if (parts.size != 2) return null
        val month = parts[0].toIntOrNull() ?: return null
        val day = parts[1].toIntOrNull() ?: return null
        if (month !in 1..12) return null
        val thisYearDate = runCatching { LocalDate.of(today.year, month, day) }.getOrNull() ?: return null
        return if (!thisYearDate.isBefore(today)) thisYearDate
        else runCatching { LocalDate.of(today.year + 1, month, day) }.getOrNull()
    }

    fun toggleHoliday(holiday: Holiday) {
        viewModelScope.launch {
            holidayRepository.updateHoliday(holiday.copy(enabled = !holiday.enabled))
        }
    }

    fun syncSquadToAll(holidayId: Long, includeSquad: Boolean = false) {
        viewModelScope.launch {
            holidayRepository.syncContactsToAllHolidays(holidayId, includeSquad)
            _uiState.update { it.copy(showConfetti = true) }
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(showConfetti = false) }
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

    fun polishMessage(currentText: String, onPolished: (String) -> Unit) {
        val holiday = _uiState.value.selectedHoliday ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isPolishing = true) }
            val polished = geminiHelper.polishMessage(currentText, holiday.name)
            if (polished != null) {
                onPolished(polished)
            }
            _uiState.update { it.copy(isPolishing = false) }
        }
    }

    fun generateFromScratch(holidayName: String, onGenerated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPolishing = true) }
            val generated = geminiHelper.generateHolidayMessage(holidayName, "{name}")
            if (generated != null) {
                onGenerated(generated)
            }
            _uiState.update { it.copy(isPolishing = false) }
        }
    }

    fun dismissTemplateDialog() {
        _uiState.update { it.copy(showTemplateDialog = false) }
    }

    fun openAddHolidayDialog() {
        _uiState.update { it.copy(showAddEditDialog = true, editingHoliday = null) }
    }

    fun openEditHolidayDialog(holiday: Holiday) {
        _uiState.update { it.copy(showAddEditDialog = true, editingHoliday = holiday) }
    }

    fun dismissAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, editingHoliday = null) }
    }

    /**
     * Saves a new holiday or updates an existing one based on [HolidayUiState.editingHoliday].
     * Pass [monthDay] in "MM-dd" form for a fixed-date holiday, or null for a squad-style entry.
     */
    fun saveHoliday(name: String, monthDay: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val editing = _uiState.value.editingHoliday
            if (editing == null) {
                holidayRepository.addHoliday(trimmed, monthDay)
            } else {
                holidayRepository.updateHoliday(
                    editing.copy(name = trimmed, monthDay = monthDay, isVariable = false)
                )
            }
            _uiState.update { it.copy(showAddEditDialog = false, editingHoliday = null) }
        }
    }

    fun deleteHoliday(holiday: Holiday) {
        // Guard: never delete a seeded holiday — it would be re-seeded on next launch
        // anyway, and we'd lose the user's contact links and template silently.
        if (holiday.id <= LAST_SEEDED_HOLIDAY_ID) return
        viewModelScope.launch {
            holidayRepository.deleteHoliday(holiday)
        }
    }
}
