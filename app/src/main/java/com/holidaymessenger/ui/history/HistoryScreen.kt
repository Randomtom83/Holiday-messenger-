package com.holidaymessenger.ui.history

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.holidaymessenger.data.db.entity.MessageLog
import com.holidaymessenger.data.db.entity.MessageStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Festive History ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text("✨", style = MaterialTheme.typography.titleLarge)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
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
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No history yet",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "When you start sending festive messages, they'll show up here.",
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

                items(logs) { log ->
                    ModernHistoryCard(log)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ModernHistoryCard(log: MessageLog) {
    val timeStr = Instant.ofEpochMilli(log.sentAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        log.contactName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        log.contactName, 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val statusColor = when (log.status) {
                    MessageStatus.SENT -> Color(0xFF4CAF50)
                    MessageStatus.FAILED -> MaterialTheme.colorScheme.error
                    MessageStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (log.status == MessageStatus.SENT) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp), tint = statusColor)
                        }
                        Text(
                            log.status.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = statusColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    log.message,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "via ${log.channel.name}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
