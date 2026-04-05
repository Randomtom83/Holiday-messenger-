package com.holidaymessenger.ui.holidays

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.holidaymessenger.data.db.entity.Holiday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayScreen(
    onPickContacts: (Long) -> Unit,
    viewModel: HolidayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Holidays") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(uiState.holidays) { holiday ->
                HolidayCard(
                    holiday = holiday,
                    onToggle = { viewModel.toggleHoliday(holiday) },
                    onEditTemplate = { viewModel.editTemplate(holiday) },
                    onPickContacts = { onPickContacts(holiday.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Template edit dialog
    if (uiState.showTemplateDialog) {
        TemplateEditDialog(
            holidayName = uiState.selectedHoliday?.name ?: "",
            currentText = uiState.editingTemplate?.text ?: "",
            onSave = { viewModel.saveTemplate(it) },
            onDismiss = { viewModel.dismissTemplateDialog() }
        )
    }
}

@Composable
private fun HolidayCard(
    holiday: Holiday,
    onToggle: () -> Unit,
    onEditTemplate: () -> Unit,
    onPickContacts: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    holiday.name,
                    style = MaterialTheme.typography.titleSmall
                )
                if (holiday.monthDay != null) {
                    Text(
                        holiday.monthDay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Variable date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEditTemplate) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit template")
            }

            IconButton(onClick = onPickContacts) {
                Icon(Icons.Filled.People, contentDescription = "Pick contacts")
            }

            Switch(
                checked = holiday.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun TemplateEditDialog(
    holidayName: String,
    currentText: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$holidayName Template") },
        text = {
            Column {
                Text(
                    "Use {name} to insert the contact's name.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
