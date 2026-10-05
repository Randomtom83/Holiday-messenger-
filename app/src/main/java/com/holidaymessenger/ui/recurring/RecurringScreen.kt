package com.holidaymessenger.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.holidaymessenger.worker.MessageSenderWorker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    onAddNew: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recurring Messages") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) {
                Icon(Icons.Filled.Add, contentDescription = "Add recurring message")
            }
        }
    ) { padding ->
        if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No recurring messages set up yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap + to create one!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(uiState.messages) { item ->
                    RecurringMessageCard(
                        item = item,
                        onToggle = { viewModel.toggleEnabled(item.scheduledMessage) },
                        onEdit = { onEdit(item.scheduledMessage.id) },
                        onDelete = { viewModel.deleteMessage(item.scheduledMessage) },
                        onTest = {
                            val req = OneTimeWorkRequestBuilder<MessageSenderWorker>()
                                .setInputData(workDataOf(MessageSenderWorker.KEY_SCHEDULED_MESSAGE_ID to item.scheduledMessage.id))
                                .build()
                            WorkManager.getInstance(context).enqueue(req)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringMessageCard(
    item: RecurringMessageItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    val msg = item.scheduledMessage
    val windowStart = formatMinutes(msg.windowStartMinutes)
    val windowEnd = formatMinutes(msg.windowEndMinutes)
    val nextFire = msg.nextScheduledTime?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
    } ?: "not scheduled"

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.contactName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    item.templateText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${msg.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} | $windowStart - $windowEnd | ${msg.channel.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Next: $nextFire",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onTest) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Test send",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Switch(
                checked = msg.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

private fun formatMinutes(totalMinutes: Int): String {
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
