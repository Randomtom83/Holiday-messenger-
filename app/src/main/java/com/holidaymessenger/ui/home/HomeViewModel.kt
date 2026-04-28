package com.holidaymessenger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.db.entity.MessageLog
import com.holidaymessenger.data.db.entity.MessageStatus
import com.holidaymessenger.data.db.entity.ScheduledMessage
import com.holidaymessenger.data.repository.ContactRepository
import com.holidaymessenger.data.repository.HolidayRepository
import com.holidaymessenger.data.repository.MessageRepository
import com.holidaymessenger.util.HolidayCalendar
import com.holidaymessenger.util.JoyMeterRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class UpcomingSend(
    val contactName: String,
    val label: String,
    val scheduledTime: Long
)

data class HomeUiState(
    val recentMessages: List<MessageLog> = emptyList(),
    val upcomingMessages: List<ScheduledMessage> = emptyList(),
    val totalScheduled: Int = 0,
    val nextHoliday: HolidayCalendar.HolidayDate? = null,
    val daysToNextHoliday: Long = 0,
    val joyMeterProgress: Float = 0f,
    val totalSquadMembers: Int = 0,
    val isBoosted: Boolean = false,
    val joyBreakdown: JoyMeterRules.Breakdown = JoyMeterRules.Breakdown(0, 0, 0),
    val joyScore: Int = 0,
    val upcomingSends: List<UpcomingSend> = emptyList(),
    val streak: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val holidayRepository: HolidayRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _boostTrigger = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        messageRepository.getRecentLogs(50),
        messageRepository.getAllScheduledMessages(),
        contactRepository.getAllContacts(),
        holidayRepository.getAllHolidays(),
        _refreshTrigger
    ) { logs, scheduled, squad, holidays, _ ->
        val nextHoliday = HolidayCalendar.getNextHoliday()
        val daysTo = ChronoUnit.DAYS.between(LocalDate.now(), nextHoliday.date)

        // Squad size based on distinct contacts across holiday cross refs (approximate via saved contacts)
        val squadCount = squad.size
        val enabledHolidayCount = holidays.count { it.enabled }
        val sentCount = logs.count { it.status == MessageStatus.SENT }

        val breakdown = JoyMeterRules.compute(squadCount, enabledHolidayCount, sentCount)

        // Boosted if something sent in the last hour
        val lastMessageTime = logs.firstOrNull()?.sentAt ?: 0L
        val isRecentlySent = System.currentTimeMillis() - lastMessageTime < 3600000

        // Upcoming sends — next 3 enabled with a nextScheduledTime
        val now = System.currentTimeMillis()
        val upcoming = scheduled
            .filter { it.enabled && (it.nextScheduledTime ?: 0L) > now }
            .sortedBy { it.nextScheduledTime ?: Long.MAX_VALUE }
            .take(3)

        val upcomingSends = upcoming.mapNotNull { sm ->
            val contact = contactRepository.getContactById(sm.contactId) ?: return@mapNotNull null
            val label = when {
                sm.holidayId != null -> holidayRepository.getHolidayById(sm.holidayId)?.name ?: "Holiday"
                sm.type == com.holidaymessenger.data.db.entity.MessageType.BIRTHDAY -> "Birthday"
                sm.type == com.holidaymessenger.data.db.entity.MessageType.RECURRING -> "Recurring"
                else -> "Scheduled"
            }
            UpcomingSend(
                contactName = contact.name,
                label = label,
                scheduledTime = sm.nextScheduledTime ?: 0L
            )
        }

        // Streak: count consecutive enabled holidays (sorted by date ASC, past ones only)
        // where at least one SENT log exists on that date.
        val streak = computeStreak(holidays.filter { it.enabled }.map { it.name }, logs)

        HomeUiState(
            recentMessages = logs.take(10),
            upcomingMessages = scheduled.filter { it.enabled },
            totalScheduled = scheduled.count { it.enabled },
            nextHoliday = nextHoliday,
            daysToNextHoliday = daysTo,
            joyMeterProgress = breakdown.percent,
            joyBreakdown = breakdown,
            joyScore = breakdown.total,
            totalSquadMembers = squadCount,
            isBoosted = isRecentlySent,
            upcomingSends = upcomingSends,
            streak = streak
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        refreshHolidays()
    }

    fun refreshHolidays() {
        viewModelScope.launch {
            holidayRepository.seedDefaultHolidays()
            _refreshTrigger.value += 1
        }
    }

    private fun computeStreak(enabledHolidayNames: List<String>, logs: List<MessageLog>): Int {
        if (enabledHolidayNames.isEmpty() || logs.isEmpty()) return 0
        val today = LocalDate.now()
        // Get this year's holidays up to today, ordered DESC
        val holidaysThisYear = HolidayCalendar.getHolidaysForYear(today.year)
            .filter { it.name in enabledHolidayNames && !it.date.isAfter(today) }
            .sortedByDescending { it.date }

        val sentDates: Set<LocalDate> = logs
            .filter { it.status == MessageStatus.SENT }
            .map { Instant.ofEpochMilli(it.sentAt).atZone(ZoneId.systemDefault()).toLocalDate() }
            .toSet()

        var count = 0
        for (h in holidaysThisYear) {
            if (sentDates.contains(h.date)) count++ else break
        }
        return count
    }
}
