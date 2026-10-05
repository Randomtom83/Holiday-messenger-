package com.holidaymessenger.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.holidaymessenger.data.db.entity.Channel
import com.holidaymessenger.data.db.entity.Frequency
import com.holidaymessenger.ui.components.FestiveDialog
import com.holidaymessenger.worker.MessageSenderWorker

@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = "Select time",
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onConfirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringEditScreen(
    scheduledMessageId: Long?,
    onBack: () -> Unit,
    viewModel: RecurringEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val selectedDays = remember { mutableStateListOf(true, true, true, true, true, true, true) }
    var showTestConfirm by remember { mutableStateOf(false) }

    if (showTestConfirm) {
        FestiveDialog(
            title = "Test Send Now?",
            onDismiss = { showTestConfirm = false },
            confirmLabel = "Send",
            onConfirm = {
                val id = scheduledMessageId
                if (id != null) {
                    val req = OneTimeWorkRequestBuilder<MessageSenderWorker>()
                        .setInputData(workDataOf(MessageSenderWorker.KEY_SCHEDULED_MESSAGE_ID to id))
                        .build()
                    WorkManager.getInstance(context).enqueue(req)
                }
                showTestConfirm = false
            }
        ) {
            Text("This will immediately fire the message worker for this entry.")
        }
    }

    LaunchedEffect(scheduledMessageId) {
        viewModel.initialize(scheduledMessageId)
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.windowStartMinutes / 60,
            initialMinute = uiState.windowStartMinutes % 60
        )
        TimePickerDialog(
            onCancel = { showStartTimePicker = false },
            onConfirm = {
                viewModel.setTime(timePickerState.hour, timePickerState.minute, true)
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.windowEndMinutes / 60,
            initialMinute = uiState.windowEndMinutes % 60
        )
        TimePickerDialog(
            onCancel = { showEndTimePicker = false },
            onConfirm = {
                viewModel.setTime(timePickerState.hour, timePickerState.minute, false)
                showEndTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (scheduledMessageId != null) "Edit Message" else "New Recurring Message") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Contact selector
            OutlinedTextField(
                value = uiState.contactName,
                onValueChange = {},
                label = { Text("Contact") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Contact is selected from the contacts screen") }
            )

            // Message text
            OutlinedTextField(
                value = uiState.messageText,
                onValueChange = { viewModel.setMessageText(it) },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                supportingText = { Text("Use {name} to insert contact's name") }
            )

            // Frequency
            Text("Frequency", style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Frequency.entries.filter { it != Frequency.ONCE && it != Frequency.YEARLY }.forEach { freq ->
                    FilterChip(
                        selected = uiState.frequency == freq,
                        onClick = { viewModel.setFrequency(freq) },
                        label = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Channel
            Text("Send via", style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Channel.entries.forEach { ch ->
                    FilterChip(
                        selected = uiState.channel == ch,
                        onClick = { viewModel.setChannel(ch) },
                        label = { Text(ch.name) }
                    )
                }
            }

            // Time window
            Text("Time Window (randomized)", style = MaterialTheme.typography.titleSmall)
            Text(
                "The message will be sent at a random time between these two times each day.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formatMinutesForInput(uiState.windowStartMinutes),
                    onValueChange = { },
                    label = { Text("Start") },
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { showStartTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Select Start Time")
                        }
                    }
                )
                OutlinedTextField(
                    value = formatMinutesForInput(uiState.windowEndMinutes),
                    onValueChange = { },
                    label = { Text("End") },
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { showEndTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Select End Time")
                        }
                    }
                )
            }

            // Day-of-week toggles
            Text("Days", style = MaterialTheme.typography.titleSmall)
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dayLabels.forEachIndexed { i, label ->
                    FilterChip(
                        selected = selectedDays[i],
                        onClick = { selectedDays[i] = !selectedDays[i] },
                        label = { Text(label) }
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    viewModel.save()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.messageText.isNotBlank()
            ) {
                Text("Save")
            }

            // Test Send Now
            OutlinedButton(
                onClick = { showTestConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = scheduledMessageId != null
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Test Send Now")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatMinutesForInput(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val amPm = if (hours < 12) "AM" else "PM"
    val displayHour = when {
        hours == 0 -> 12
        hours > 12 -> hours - 12
        else -> hours
    }
    return "$displayHour:${minutes.toString().padStart(2, '0')} $amPm"
}
