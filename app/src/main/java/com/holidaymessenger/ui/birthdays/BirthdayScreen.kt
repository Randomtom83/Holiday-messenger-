package com.holidaymessenger.ui.birthdays

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.holidaymessenger.ui.components.FestiveDialog
import com.holidaymessenger.ui.components.MessagePreviewDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayScreen(
    viewModel: BirthdayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Birthday Bash ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE91E63)
                            )
                        )
                        Text("🎂", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        containerColor = Color(0xFFE91E63).copy(alpha = 0.05f)
    ) { padding ->
        if (uiState.birthdayContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Cake,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No birthdays found",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add contacts first, or set birthdays manually from the contacts screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(uiState.birthdayContacts) { bc ->
                    ModernBirthdayCard(
                        birthdayContact = bc,
                        onToggle = { viewModel.toggleBirthdayMessage(bc) },
                        onEditBirthday = { viewModel.editBirthday(bc.contact) },
                        onEditTemplate = { viewModel.editTemplate(bc.contact) }
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
    if (uiState.showTemplateDialog) {
        BirthdayTemplateDialog(
            contactName = uiState.editingContact?.name ?: "",
            currentTemplate = uiState.templateInput,
            onSave = { viewModel.saveTemplate(it) },
            onDismiss = { viewModel.dismissTemplateDialog() }
        )
    }
}

@Composable
private fun ModernBirthdayCard(
    birthdayContact: BirthdayContact,
    onToggle: () -> Unit,
    onEditBirthday: () -> Unit,
    onEditTemplate: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (birthdayContact.isEnabled) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "BirthdayScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (birthdayContact.isEnabled)
                Color.White
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = if (birthdayContact.isEnabled) CardDefaults.cardElevation(8.dp) else CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (birthdayContact.isEnabled) Color(0xFFE91E63).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (birthdayContact.isEnabled) {
                    Icon(Icons.Default.Celebration, null, tint = Color(0xFFE91E63))
                } else {
                    Text(
                        birthdayContact.contact.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    birthdayContact.contact.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (birthdayContact.isEnabled) Color(0xFFE91E63) else Color.Unspecified
                    )
                )
                Text(
                    birthdayContact.contact.effectiveBirthday ?: "No birthday set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditTemplate) {
                Icon(
                    Icons.AutoMirrored.Filled.Message,
                    contentDescription = "Edit template",
                    tint = Color(0xFFE91E63).copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onEditBirthday) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit birthday",
                    tint = Color(0xFFE91E63).copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Switch(
                checked = birthdayContact.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFE91E63),
                    checkedTrackColor = Color(0xFFE91E63).copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun BirthdayTemplateDialog(
    contactName: String,
    currentTemplate: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentTemplate) }
    var showPreview by remember { mutableStateOf(false) }

    FestiveDialog(
        title = "Custom Birthday for $contactName",
        onDismiss = onDismiss,
        confirmLabel = "Save",
        onConfirm = { onSave(text) }
    ) {
        Column {
            Text(
                "Personalize the birthday message for this contact. Use {name} for their name.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Message Template") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showPreview = true }) {
                Text("Preview")
            }
        }
    }

    if (showPreview) {
        MessagePreviewDialog(
            template = text,
            sampleNames = listOf(contactName.ifBlank { "Alex" }),
            onDismiss = { showPreview = false }
        )
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

    FestiveDialog(
        title = "$contactName's Birthday",
        onDismiss = onDismiss,
        confirmLabel = "Save",
        onConfirm = { onSave(text) }
    ) {
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
    }
}
