package com.holidaymessenger.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.holidaymessenger.data.db.entity.Channel
import com.holidaymessenger.data.db.entity.Frequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringEditScreen(
    scheduledMessageId: Long?,
    onBack: () -> Unit,
    viewModel: RecurringEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(scheduledMessageId) {
        viewModel.initialize(scheduledMessageId)
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
                    onValueChange = { viewModel.parseAndSetStartTime(it) },
                    label = { Text("Start") },
                    modifier = Modifier.weight(1f),
                    supportingText = { Text("e.g., 11:00 AM") }
                )
                OutlinedTextField(
                    value = formatMinutesForInput(uiState.windowEndMinutes),
                    onValueChange = { viewModel.parseAndSetEndTime(it) },
                    label = { Text("End") },
                    modifier = Modifier.weight(1f),
                    supportingText = { Text("e.g., 2:00 PM") }
                )
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
