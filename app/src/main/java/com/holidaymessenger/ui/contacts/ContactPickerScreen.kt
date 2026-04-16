package com.holidaymessenger.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.holidaymessenger.data.contacts.ContactsProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerScreen(
    holidayId: Long?,
    onBack: () -> Unit,
    viewModel: ContactPickerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.values.all { it }
            hasPermissions = allGranted
            if (allGranted) {
                viewModel.initialize(holidayId)
            }
        }
    )

    LaunchedEffect(holidayId, hasPermissions) {
        if (hasPermissions) {
            viewModel.initialize(holidayId)
        } else {
            launcher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All?") },
            text = { Text("Remove all contacts from ${if (holidayId == null || holidayId == 0L) "the Squad" else "this holiday"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllContacts()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Keep them")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (uiState.showLateMessageWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLateWarning() },
            title = { Text("Better Late Than Never! 🐢✨") },
            text = { 
                Text("It looks like ${uiState.lateHolidayName} has already passed, but the festive spirit doesn't have an expiration date! You can still send your messages now.")
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissLateWarning() }) {
                    Text("Let's Spread the Joy!")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (uiState.showBlastDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeBlastDialog() },
            title = {
                Text(
                    "Send a Festive Blast! 🚀",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        "Message everyone in the Squad right now!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.blastMessage,
                        onValueChange = { viewModel.updateBlastMessage(it) },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        label = { Text("Your Message") },
                        placeholder = { Text("Use {name} for personalization") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.polishBlastMessage() },
                        enabled = !uiState.isPolishing,
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isPolishing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("AI Magic Polish")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.sendBlast() },
                    enabled = !uiState.isSending && uiState.blastMessage.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Send to All! ✨")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeBlastDialog() }) {
                    Text("Not now")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (holidayId == null || holidayId == 0L) "The Squad" else "Select Contacts",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (uiState.selectedContactIds.isNotEmpty()) {
                            Text(
                                "🔥 ${uiState.selectedContactIds.size} in the squad",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter/Sort")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!hasPermissions) {
                PermissionDeniedView(onGrant = { 
                    launcher.launch(arrayOf(Manifest.permission.READ_CONTACTS)) 
                })
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modern Search Bar
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) }
                    )

                    // Google Contacts labels → filter chips
                    if (uiState.contactGroups.isNotEmpty()) {
                        GroupFilterChips(
                            groups = uiState.contactGroups,
                            selectedGroupIds = uiState.selectedGroupIds,
                            onToggle = { viewModel.toggleGroupFilter(it) },
                            onClear = { viewModel.clearGroupFilter() },
                            onSelectAllFiltered = { viewModel.selectAllFiltered() },
                            filteredCount = viewModel.getFilteredContacts().size,
                            unselectedFilteredCount = viewModel.getFilteredContacts()
                                .count { !uiState.selectedContactIds.contains(it.id) }
                        )
                    }

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                        }
                    } else {
                        val filteredContacts = viewModel.getFilteredContacts()
                        
                        if (filteredContacts.isEmpty()) {
                            EmptyContactsView(uiState.searchQuery)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                if (holidayId == null) {
                                    item {
                                        Text(
                                            "Your Squad is the ultimate list of people who deserve festive magic! ✨",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                items(filteredContacts, key = { it.id }) { contact ->
                                    val isSelected = uiState.selectedContactIds.contains(contact.id)
                                    val savedContact = uiState.savedContacts.find { it.id == contact.id }

                                    ModernContactItem(
                                        contact = contact,
                                        isSelected = isSelected,
                                        preferredChannel = savedContact?.preferredChannel ?: com.holidaymessenger.data.db.entity.Channel.SMS,
                                        onClick = { 
                                            android.util.Log.d("ContactPicker", "Toggling contact: ${contact.id} (${contact.name})")
                                            viewModel.toggleContact(contact.id) 
                                        },
                                        onChannelChange = { viewModel.updateContactChannel(contact.id, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Confetti Overlay
            if (uiState.showConfetti) {
                ConfettiOverlay()
            }
            
            // Floating Action Buttons
            if (uiState.selectedContactIds.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openBlastDialog() },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        shape = RoundedCornerShape(24.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Blast Message 🚀",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.applyToAllHolidays() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Sync Squad",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }

    // Sort Menu
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false },
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Sort By", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        DropdownMenuItem(
            text = { Text("Selected 🌟") },
            leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
            onClick = { viewModel.setSortOrder(ContactSortOrder.SELECTED); showSortMenu = false }
        )
        DropdownMenuItem(
            text = { Text("A-Z (Alphabetical)") },
            leadingIcon = { Icon(Icons.Default.SortByAlpha, null) },
            onClick = { viewModel.setSortOrder(ContactSortOrder.ALPHABETICAL); showSortMenu = false }
        )
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        placeholder = { Text("Search your contacts...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFilterChips(
    groups: List<ContactsProvider.ContactGroup>,
    selectedGroupIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onClear: () -> Unit,
    onSelectAllFiltered: () -> Unit,
    filteredCount: Int,
    unselectedFilteredCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedGroupIds.isNotEmpty()) {
                item {
                    AssistChip(
                        onClick = onClear,
                        label = { Text("Clear") },
                        leadingIcon = {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            items(groups, key = { it.id }) { group ->
                val selected = selectedGroupIds.contains(group.id)
                FilterChip(
                    selected = selected,
                    onClick = { onToggle(group.id) },
                    label = { Text("${group.title} · ${group.memberCount}") },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Bulk-select action bar — only appears while a label filter is active
        if (selectedGroupIds.isNotEmpty() && filteredCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$filteredCount matching",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (unselectedFilteredCount > 0) {
                    TextButton(onClick = onSelectAllFiltered) {
                        Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Select all $unselectedFilteredCount")
                    }
                }
            }
        }
    }
}

@Composable
fun ModernContactItem(
    contact: ContactsProvider.DeviceContact,
    isSelected: Boolean,
    preferredChannel: com.holidaymessenger.data.db.entity.Channel,
    onClick: () -> Unit,
    onChannelChange: (com.holidaymessenger.data.db.entity.Channel) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ContactScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.White,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact Avatar/Initial
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = Color.White)
                    } else if (contact.isGroup) {
                        Icon(
                            Icons.Default.Groups, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            contact.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            contact.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (contact.birthday != null) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Cake, 
                                contentDescription = "Has Birthday", 
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        if (contact.isGroup) "Group Chat" else contact.phoneNumber ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isSelected) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isSelected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Channel:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = preferredChannel == com.holidaymessenger.data.db.entity.Channel.SMS,
                            onClick = { onChannelChange(com.holidaymessenger.data.db.entity.Channel.SMS) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("SMS/MMS")
                        }
                        SegmentedButton(
                            selected = preferredChannel == com.holidaymessenger.data.db.entity.Channel.WHATSAPP,
                            onClick = { onChannelChange(com.holidaymessenger.data.db.entity.Channel.WHATSAPP) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            enabled = !contact.isGroup
                        ) {
                            Text("WhatsApp")
                        }
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

@Composable
fun EmptyContactsView(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PersonOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text(
            if (query.isEmpty()) "No contacts found" else "No results for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PermissionDeniedView(onGrant: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(Icons.Default.ContactPhone, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Access your contacts",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Holiday Messenger needs contact permission so you can easily choose who to spread joy to!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGrant,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Grant Permission")
        }
    }
}
