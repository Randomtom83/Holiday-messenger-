package com.holidaymessenger.ui.holidays

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.ui.components.MessagePreviewDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayScreen(
    onPickContacts: (Long) -> Unit,
    viewModel: HolidayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨ ", fontSize = 24.sp)
                        Text(
                            "Festive Magic",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(" 🎄", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddHolidayDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add", fontWeight = FontWeight.ExtraBold)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Festive Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            )
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { 
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your Holiday Squads",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        "Tap a card to start the festive magic! 🪄",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                items(uiState.holidays) { holiday ->
                    FestiveHolidayCard(
                        holiday = holiday,
                        onToggle = { viewModel.toggleHoliday(holiday) },
                        onEditTemplate = { viewModel.editTemplate(holiday) },
                        onPickContacts = { onPickContacts(holiday.id) },
                        onSyncSquad = { includeSquad -> viewModel.syncSquadToAll(holiday.id, includeSquad) },
                        onEditHoliday = { viewModel.openEditHolidayDialog(holiday) },
                        onDeleteHoliday = { viewModel.deleteHoliday(holiday) }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Confetti Animation Overlay
    if (uiState.showConfetti) {
        ConfettiOverlay()
    }

    // Template edit dialog
    if (uiState.showTemplateDialog) {
        FestiveWizardDialog(
            holidayName = uiState.selectedHoliday?.name ?: "",
            currentText = uiState.editingTemplate?.text ?: "",
            isPolishing = uiState.isPolishing,
            onPolish = { text, onPolished -> viewModel.polishMessage(text, onPolished) },
            onGenerate = { holidayName, onGenerated -> viewModel.generateFromScratch(holidayName, onGenerated) },
            onSave = { viewModel.saveTemplate(it) },
            onDismiss = { viewModel.dismissTemplateDialog() }
        )
    }

    // Add / edit-holiday dialog
    if (uiState.showAddEditDialog) {
        AddEditHolidayDialog(
            existing = uiState.editingHoliday,
            onSave = { name, monthDay -> viewModel.saveHoliday(name, monthDay) },
            onDismiss = { viewModel.dismissAddEditDialog() }
        )
    }
}

@Composable
private fun FestiveHolidayCard(
    holiday: Holiday,
    onToggle: () -> Unit,
    onEditTemplate: () -> Unit,
    onPickContacts: () -> Unit,
    onSyncSquad: (Boolean) -> Unit,
    onEditHoliday: () -> Unit,
    onDeleteHoliday: () -> Unit
) {
    var showSyncConfirm by remember { mutableStateOf(false) }
    var includeSquadInSync by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val canDelete = holiday.id > LAST_SEEDED_HOLIDAY_ID

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${holiday.name}?") },
            text = { Text("This removes the holiday and its contact list. The message template stays in case you re-create it later.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHoliday()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showSyncConfirm) {
        AlertDialog(
            onDismissRequest = { showSyncConfirm = false },
            title = { Text("Sync the Whole Squad? 🔄") },
            text = { 
                Column {
                    Text("This copies your contact list from ${holiday.name} to EVERY other holiday. Ultimate efficiency! 🚀✨")
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { includeSquadInSync = !includeSquadInSync }
                    ) {
                        Checkbox(
                            checked = includeSquadInSync,
                            onCheckedChange = { includeSquadInSync = it }
                        )
                        Text("Also update 'The Squad' (Master List)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSyncSquad(includeSquadInSync)
                        showSyncConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Make it Happen!")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncConfirm = false }) {
                    Text("Maybe later")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPickContacts() },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (holiday.enabled) 
                MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Festive Icon
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = if (holiday.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            getEmojiForHoliday(holiday.name),
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        holiday.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (holiday.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatHolidayDateLabel(holiday),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onEditHoliday) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                if (canDelete) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Switch(
                    checked = holiday.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            if (holiday.enabled) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onEditTemplate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Magic Text", fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { showSyncConfirm = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Sync, "Sync", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiOverlay() {
    val emojis = listOf("✨", "🎉", "🎊", "🌟", "🔥", "🚀", "🪄", "🎄", "🎅", "🦌", "🍪")
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(40) { i ->
            val infiniteTransition = rememberInfiniteTransition(label = "confetti_$i")
            val xOffset = remember { (0..1000).random() / 1000f }
            val delay = remember { (0..3000).random() }
            val duration = remember { (1500..3000).random() }
            
            val yPos by infiniteTransition.animateFloat(
                initialValue = -0.1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "yPos"
            )
            
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 720f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration / 2, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Text(
                text = emojis[i % emojis.size],
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(start = (xOffset * 360).dp)
                    .graphicsLayer {
                        translationY = yPos * 3000
                        rotationZ = rotation
                        scaleX = scale
                        scaleY = scale
                    },
                fontSize = (16..32).random().sp
            )
        }
    }
}

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun formatHolidayDateLabel(holiday: Holiday): String {
    val md = holiday.monthDay
    return when {
        holiday.isVariable -> "Variable ✨"
        md == null -> "No date — manual squad"
        else -> {
            val parts = md.split("-")
            if (parts.size == 2) {
                val month = parts[0].toIntOrNull()
                val day = parts[1].toIntOrNull()
                if (month != null && day != null && month in 1..12) {
                    "${MONTH_NAMES[month - 1]} $day"
                } else md
            } else md
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditHolidayDialog(
    existing: Holiday?,
    onSave: (name: String, monthDay: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = existing != null
    var name by remember { mutableStateOf(existing?.name ?: "") }

    val initialMonthDay = existing?.monthDay
    val initialMonth = initialMonthDay?.split("-")?.getOrNull(0)?.toIntOrNull()
    val initialDay = initialMonthDay?.split("-")?.getOrNull(1)?.toIntOrNull()

    var hasDate by remember { mutableStateOf(initialMonthDay != null) }
    var month by remember { mutableIntStateOf(initialMonth ?: 1) }
    var day by remember { mutableIntStateOf(initialDay ?: 1) }
    var monthMenuOpen by remember { mutableStateOf(false) }
    var dayMenuOpen by remember { mutableStateOf(false) }

    fun maxDayForMonth(m: Int): Int = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> 29 // allow Feb 29 for leap-year birthdays / holidays
        else -> 31
    }

    // Clamp day if user shrinks the month after picking a high day
    LaunchedEffect(month) {
        val cap = maxDayForMonth(month)
        if (day > cap) day = cap
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) "Edit ${existing?.name}" else "New Holiday or Squad",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., Alpha Sigma Phi Founders' Day") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Has a specific date?",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            if (hasDate) "Triggers automatically each year" else "Squad-style — manual sends only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = hasDate, onCheckedChange = { hasDate = it })
                }

                if (hasDate) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = monthMenuOpen,
                            onExpandedChange = { monthMenuOpen = !monthMenuOpen },
                            modifier = Modifier.weight(2f)
                        ) {
                            OutlinedTextField(
                                value = MONTH_NAMES[month - 1],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Month") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuOpen) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = monthMenuOpen,
                                onDismissRequest = { monthMenuOpen = false }
                            ) {
                                MONTH_NAMES.forEachIndexed { idx, mName ->
                                    DropdownMenuItem(
                                        text = { Text(mName) },
                                        onClick = {
                                            month = idx + 1
                                            monthMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = dayMenuOpen,
                            onExpandedChange = { dayMenuOpen = !dayMenuOpen },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = day.toString(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Day") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayMenuOpen) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = dayMenuOpen,
                                onDismissRequest = { dayMenuOpen = false }
                            ) {
                                (1..maxDayForMonth(month)).forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.toString()) },
                                        onClick = {
                                            day = d
                                            dayMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val md = if (hasDate) "%02d-%02d".format(month, day) else null
                    onSave(name, md)
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isEditing) "Save" else "Create ✨")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

private fun getEmojiForHoliday(name: String): String {
    return when {
        name.contains("New Year") -> "🎆"
        name.contains("Valentine") -> "💖"
        name.contains("Easter") -> "🥚"
        name.contains("Passover") -> "🍷"
        name.contains("Mother") -> "💐"
        name.contains("Father") -> "👔"
        name.contains("Juneteenth") -> "✊🏾"
        name.contains("Eid") -> "🌙"
        name.contains("July") -> "🇺🇸"
        name.contains("Rosh") -> "🍎"
        name.contains("Halloween") -> "🎃"
        name.contains("Election") -> "🗳️"
        name.contains("Diwali") -> "🪔"
        name.contains("Thanksgiving") -> "🦃"
        name.contains("Hanukkah") -> "🕎"
        name.contains("Christmas") -> "🎄"
        else -> "🎉"
    }
}

@Composable
private fun FestiveWizardDialog(
    holidayName: String,
    currentText: String,
    isPolishing: Boolean,
    onPolish: (String, (String) -> Unit) -> Unit,
    onGenerate: (String, (String) -> Unit) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var text by remember { mutableStateOf(currentText) }
    var showPreview by remember { mutableStateOf(false) }

    if (showPreview) {
        MessagePreviewDialog(
            template = text,
            sampleNames = listOf("Alex", "Jordan", "Sam"),
            onDismiss = { showPreview = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Indicator
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) { i ->
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step > i) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.primaryContainer
                                )
                        )
                    }
                }

                AnimatedContent(targetState = step, label = "wizard_step") { currentStep ->
                    when (currentStep) {
                        1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Step 1: The Message ✍️",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                placeholder = { Text("Write your $holidayName greeting...") },
                                shape = RoundedCornerShape(20.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            // Festive Polish Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        step = 2
                                        onPolish(text) { text = it } 
                                    },
                                    enabled = !isPolishing && text.isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    if (isPolishing) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("AI Polish ✨")
                                    }
                                }

                                Button(
                                    onClick = {
                                        step = 2
                                        onGenerate(holidayName) { text = it }
                                    },
                                    enabled = !isPolishing,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    if (isPolishing) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Celebration, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Surprise!")
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { showPreview = true }, enabled = text.isNotBlank()) {
                                Text("Preview")
                            }
                        }
                        2 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Step 2: Preview & Finish 🎁",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            
                            if (isPolishing) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(8.dp))
                                        Text("AI is weaving magic...", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            "To: Thomas",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text.replace("{name}", "Thomas"),
                                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Tip: Use {name} to personalize each message automatically!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 2) step++ else onSave(text)
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(if (step < 2) "Next Step ➡️" else "Save & Set Magic ✨")
            }
        },
        dismissButton = {
            if (step > 1) {
                TextButton(onClick = { step-- }) {
                    Text("Back")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(32.dp)
    )
}
