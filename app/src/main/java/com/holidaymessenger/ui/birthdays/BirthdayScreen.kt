package com.holidaymessenger.ui.birthdays

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayScreen(
    viewModel: BirthdayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Birthdays") })
        }
    ) { padding ->
        if (uiState.birthdayContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No contacts with birthdays found.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add contacts first, or set birthdays manually\nfrom the contacts screen.",
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

                items(uiState.birthdayContacts) { bc ->
                    BirthdayCard(
                        birthdayContact = bc,
                        onToggle = { viewModel.toggleBirthdayMessage(bc) },
                        onEditBirthday = { viewModel.editBirthday(bc.contact) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (uiState.showBirthdayDialog) {
        BirthdayEditDialog(
            contactName = uiState.editingContact?.name ?: "",
            currentBirthday = uiState.birthdayInput,
            onSave = { viewModel.saveBirthday(it) },
            onDismiss = { viewModel.dismissBirthdayDialog() }
        )
    }
}

@Composable
private fun BirthdayCard(
    birthdayContact: BirthdayContact,
    onToggle: () -> Unit,
    onEditBirthday: () -> Unit
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
                    birthdayContact.contact.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    birthdayContact.contact.effectiveBirthday ?: "No birthday set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditBirthday) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit birthday")
            }

            Switch(
                checked = birthdayContact.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun BirthdayEditDialog(
    contactName: String,
    currentBirthday: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentBirthday) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$contactName's Birthday") },
        text = {
            Column {
                Text(
                    "Enter birthday as MM-dd (e.g., 03-15 for March 15)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("MM-dd") },
                    modifier = Modifier.fillMaxWidth()
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
