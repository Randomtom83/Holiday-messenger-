package com.holidaymessenger.ui.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerScreen(
    holidayId: Long?,
    onBack: () -> Unit,
    viewModel: ContactPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(holidayId) {
        viewModel.initialize(holidayId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Contacts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.SortByAlpha, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently texted") },
                                onClick = {
                                    viewModel.setSortOrder(ContactSortOrder.RECENTLY_TEXTED)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest texted") },
                                onClick = {
                                    viewModel.setSortOrder(ContactSortOrder.OLDEST_TEXTED)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alphabetical") },
                                onClick = {
                                    viewModel.setSortOrder(ContactSortOrder.ALPHABETICAL)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search contacts...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            // Sort indicator
            Text(
                "Sorted by: ${
                    when (uiState.sortOrder) {
                        ContactSortOrder.RECENTLY_TEXTED -> "Recently texted"
                        ContactSortOrder.OLDEST_TEXTED -> "Oldest texted"
                        ContactSortOrder.ALPHABETICAL -> "A-Z"
                    }
                }",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredContacts = viewModel.getFilteredContacts()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredContacts) { contact ->
                        val isSelected = uiState.selectedContactIds.contains(contact.id)

                        ListItem(
                            headlineContent = { Text(contact.name) },
                            supportingContent = {
                                Text(contact.phoneNumber ?: "No phone number")
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleContact(contact.id) }
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.toggleContact(contact.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
